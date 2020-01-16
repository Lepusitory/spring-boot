/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.loader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.boot.loader.archive.JarFileArchive;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JarLauncher}.
 *
 * @author Andy Wilkinson
 * @author Madhura Bhave
 */
class JarLauncherTests extends AbstractExecutableArchiveLauncherTests {

	@Test
	void explodedJarHasOnlyBootInfClassesAndContentsOfBootInfLibOnClasspath() throws Exception {
		File explodedRoot = explode(createJarArchive("archive.jar", "BOOT-INF"));
		JarLauncher launcher = new JarLauncher(new ExplodedArchive(explodedRoot, true));
		List<Archive> archives = new ArrayList<>();
		launcher.getClassPathArchivesIterator().forEachRemaining(archives::add);
		assertThat(getUrls(archives)).containsExactlyInAnyOrder(getExpectedFileUrls(explodedRoot));
		for (Archive archive : archives) {
			archive.close();
		}
	}

	@Test
	void archivedJarHasOnlyBootInfClassesAndContentsOfBootInfLibOnClasspath() throws Exception {
		File jarRoot = createJarArchive("archive.jar", "BOOT-INF");
		try (JarFileArchive archive = new JarFileArchive(jarRoot)) {
			JarLauncher launcher = new JarLauncher(archive);
			List<Archive> classPathArchives = new ArrayList<>();
			launcher.getClassPathArchivesIterator().forEachRemaining(classPathArchives::add);
			assertThat(classPathArchives).hasSize(4);
			assertThat(getUrls(classPathArchives)).containsOnly(
					new URL("jar:" + jarRoot.toURI().toURL() + "!/BOOT-INF/classes!/"),
					new URL("jar:" + jarRoot.toURI().toURL() + "!/BOOT-INF/lib/foo.jar!/"),
					new URL("jar:" + jarRoot.toURI().toURL() + "!/BOOT-INF/lib/bar.jar!/"),
					new URL("jar:" + jarRoot.toURI().toURL() + "!/BOOT-INF/lib/baz.jar!/"));
			for (Archive classPathArchive : classPathArchives) {
				classPathArchive.close();
			}
		}
	}

	@Test
	void explodedJarShouldPreserveClasspathOrderWhenIndexPresent() throws Exception {
		File explodedRoot = explode(createJarArchive("archive.jar", "BOOT-INF", true));
		JarLauncher launcher = new JarLauncher(new ExplodedArchive(explodedRoot, true));
		Iterator<Archive> archives = launcher.getClassPathArchivesIterator();
		URLClassLoader classLoader = (URLClassLoader) launcher.createClassLoader(archives);
		URL[] urls = classLoader.getURLs();
		assertThat(urls).containsExactly(getExpectedFileUrls(explodedRoot));
	}

	protected final URL[] getExpectedFileUrls(File explodedRoot) {
		return getExpectedFiles(explodedRoot).stream().map(this::toUrl).toArray(URL[]::new);
	}

	protected final List<File> getExpectedFiles(File parent) {
		List<File> expected = new ArrayList<>();
		expected.add(new File(parent, "BOOT-INF/classes"));
		expected.add(new File(parent, "BOOT-INF/lib/foo.jar"));
		expected.add(new File(parent, "BOOT-INF/lib/bar.jar"));
		expected.add(new File(parent, "BOOT-INF/lib/baz.jar"));
		return expected;
	}

}
