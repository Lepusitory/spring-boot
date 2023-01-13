/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.build.bom.bomr;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.gradle.api.InvalidUserDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.build.bom.Library;
import org.springframework.boot.build.bom.Library.DependencyVersions;
import org.springframework.boot.build.bom.Library.Group;
import org.springframework.boot.build.bom.Library.Module;
import org.springframework.boot.build.bom.Library.ProhibitedVersion;
import org.springframework.boot.build.bom.Library.VersionAlignment;
import org.springframework.boot.build.bom.UpgradePolicy;
import org.springframework.boot.build.bom.bomr.version.DependencyVersion;

/**
 * Standard implementation for {@link LibraryUpdateResolver}.
 *
 * @author Andy Wilkinson
 */
class StandardLibraryUpdateResolver implements LibraryUpdateResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(StandardLibraryUpdateResolver.class);

	private final VersionResolver versionResolver;

	private final UpgradePolicy upgradePolicy;

	StandardLibraryUpdateResolver(VersionResolver versionResolver, UpgradePolicy upgradePolicy) {
		this.versionResolver = versionResolver;
		this.upgradePolicy = upgradePolicy;
	}

	@Override
	public List<LibraryWithVersionOptions> findLibraryUpdates(Collection<Library> librariesToUpgrade,
			Map<String, Library> librariesByName) {
		List<LibraryWithVersionOptions> result = new ArrayList<>();
		for (Library library : librariesToUpgrade) {
			if (isLibraryExcluded(library)) {
				continue;
			}
			LOGGER.info("Looking for updates for {}", library.getName());
			long start = System.nanoTime();
			List<VersionOption> versionOptions = getVersionOptions(library, librariesByName);
			result.add(new LibraryWithVersionOptions(library, versionOptions));
			LOGGER.info("Found {} updates for {}, took {}", versionOptions.size(), library.getName(),
					Duration.ofNanos(System.nanoTime() - start));
		}
		return result;
	}

	protected boolean isLibraryExcluded(Library library) {
		return library.getName().equals("Spring Boot");
	}

	protected List<VersionOption> getVersionOptions(Library library, Map<String, Library> libraries) {
		if (library.getVersion().getVersionAlignment() != null) {
			return determineAlignedVersionOption(library, libraries);
		}
		return determineResolvedVersionOptions(library);
	}

	private List<VersionOption> determineResolvedVersionOptions(Library library) {
		Map<String, SortedSet<DependencyVersion>> moduleVersions = new LinkedHashMap<>();
		DependencyVersion libraryVersion = library.getVersion().getVersion();
		for (Group group : library.getGroups()) {
			for (Module module : group.getModules()) {
				moduleVersions.put(group.getId() + ":" + module.getName(),
						getLaterVersionsForModule(group.getId(), module.getName(), libraryVersion));
			}
			for (String bom : group.getBoms()) {
				moduleVersions.put(group.getId() + ":" + bom,
						getLaterVersionsForModule(group.getId(), bom, libraryVersion));
			}
			for (String plugin : group.getPlugins()) {
				moduleVersions.put(group.getId() + ":" + plugin,
						getLaterVersionsForModule(group.getId(), plugin, libraryVersion));
			}
		}
		List<DependencyVersion> allVersions = moduleVersions.values().stream().flatMap(SortedSet::stream).distinct()
				.filter((dependencyVersion) -> isPermitted(dependencyVersion, library.getProhibitedVersions()))
				.collect(Collectors.toList());
		if (allVersions.isEmpty()) {
			return Collections.emptyList();
		}
		return allVersions.stream().map((version) -> new VersionOption.ResolvedVersionOption(version,
				getMissingModules(moduleVersions, version))).collect(Collectors.toList());
	}

	private List<VersionOption> determineAlignedVersionOption(Library library, Map<String, Library> libraries) {
		VersionOption alignedVersionOption = alignedVersionOption(library, libraries);
		if (alignedVersionOption == null) {
			return Collections.emptyList();
		}
		if (!isPermitted(alignedVersionOption.getVersion(), library.getProhibitedVersions())) {
			throw new InvalidUserDataException("Version alignment failed. Version " + alignedVersionOption.getVersion()
					+ " from " + library.getName() + " is prohibited");
		}
		return Collections.singletonList(alignedVersionOption);
	}

	private VersionOption alignedVersionOption(Library library, Map<String, Library> libraries) {
		VersionAlignment versionAlignment = library.getVersion().getVersionAlignment();
		Library alignmentLibrary = libraries.get(versionAlignment.getLibraryName());
		DependencyVersions dependencyVersions = alignmentLibrary.getDependencyVersions();
		if (dependencyVersions == null) {
			throw new InvalidUserDataException("Cannot align with library '" + versionAlignment.getLibraryName()
					+ "' as it does not define any dependency versions");
		}
		if (!dependencyVersions.available()) {
			return null;
		}
		Set<String> versions = new HashSet<>();
		for (Group group : library.getGroups()) {
			for (Module module : group.getModules()) {
				String version = dependencyVersions.getVersion(group.getId(), module.getName());
				if (version != null) {
					versions.add(version);
				}
			}
		}
		if (versions.isEmpty()) {
			throw new InvalidUserDataException("Cannot align with library '" + versionAlignment.getLibraryName()
					+ "' as its dependency versions do not include any of this library's modules");
		}
		if (versions.size() > 1) {
			throw new InvalidUserDataException("Cannot align with library '" + versionAlignment.getLibraryName()
					+ "' as it uses multiple different versions of this library's modules");
		}
		DependencyVersion version = DependencyVersion.parse(versions.iterator().next());
		return library.getVersion().getVersion().equals(version) ? null
				: new VersionOption.AlignedVersionOption(version, alignmentLibrary);
	}

	private boolean isPermitted(DependencyVersion dependencyVersion, List<ProhibitedVersion> prohibitedVersions) {
		for (ProhibitedVersion prohibitedVersion : prohibitedVersions) {
			String dependencyVersionToString = dependencyVersion.toString();
			if (prohibitedVersion.getRange() != null && prohibitedVersion.getRange()
					.containsVersion(new DefaultArtifactVersion(dependencyVersionToString))) {
				return false;
			}
			for (String startsWith : prohibitedVersion.getStartsWith()) {
				if (dependencyVersionToString.startsWith(startsWith)) {
					return false;
				}
			}
			for (String endsWith : prohibitedVersion.getEndsWith()) {
				if (dependencyVersionToString.endsWith(endsWith)) {
					return false;
				}
			}
			for (String contains : prohibitedVersion.getContains()) {
				if (dependencyVersionToString.contains(contains)) {
					return false;
				}
			}
		}
		return true;
	}

	private List<String> getMissingModules(Map<String, SortedSet<DependencyVersion>> moduleVersions,
			DependencyVersion version) {
		List<String> missingModules = new ArrayList<>();
		moduleVersions.forEach((name, versions) -> {
			if (!versions.contains(version)) {
				missingModules.add(name);
			}
		});
		return missingModules;
	}

	private SortedSet<DependencyVersion> getLaterVersionsForModule(String groupId, String artifactId,
			DependencyVersion currentVersion) {
		SortedSet<DependencyVersion> versions = this.versionResolver.resolveVersions(groupId, artifactId);
		versions.removeIf((candidate) -> !this.upgradePolicy.test(candidate, currentVersion));
		return versions;
	}

}
