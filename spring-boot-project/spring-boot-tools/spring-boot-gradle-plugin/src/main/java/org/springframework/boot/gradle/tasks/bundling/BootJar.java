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

package org.springframework.boot.gradle.tasks.bundling;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.Callable;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.util.ConfigureUtil;

/**
 * A custom {@link Jar} task that produces a Spring Boot executable jar.
 *
 * @author Andy Wilkinson
 * @author Madhura Bhave
 * @author Scott Frederick
 * @author Phillip Webb
 * @since 2.0.0
 */
public class BootJar extends Jar implements BootArchive {

	private static final String LAUNCHER = "org.springframework.boot.loader.JarLauncher";

	private static final String CLASSES_FOLDER = "BOOT-INF/classes/";

	private static final String LIB_FOLDER = "BOOT-INF/lib/";

	private static final String LAYERS_INDEX = "BOOT-INF/layers.idx";

	private static final String CLASSPATH_INDEX = "BOOT-INF/classpath.idx";

	private final BootArchiveSupport support;

	private final CopySpec bootInfSpec;

	private String mainClassName;

	private FileCollection classpath;

	private LayeredSpec layered;

	/**
	 * Creates a new {@code BootJar} task.
	 */
	public BootJar() {
		this.support = new BootArchiveSupport(LAUNCHER, this::isLibrary, this::resolveZipCompression);
		this.bootInfSpec = getProject().copySpec().into("BOOT-INF");
		configureBootInfSpec(this.bootInfSpec);
		getMainSpec().with(this.bootInfSpec);
	}

	private void configureBootInfSpec(CopySpec bootInfSpec) {
		bootInfSpec.into("classes", fromCallTo(this::classpathDirectories));
		bootInfSpec.into("lib", fromCallTo(this::classpathFiles)).eachFile(this.support::excludeNonZipFiles);
		bootInfSpec.filesMatching("module-info.class",
				(details) -> details.setRelativePath(details.getRelativeSourcePath()));
	}

	private Iterable<File> classpathDirectories() {
		return classpathEntries(File::isDirectory);
	}

	private Iterable<File> classpathFiles() {
		return classpathEntries(File::isFile);
	}

	private Iterable<File> classpathEntries(Spec<File> filter) {
		return (this.classpath != null) ? this.classpath.filter(filter) : Collections.emptyList();
	}

	@Override
	public void copy() {
		this.support.configureManifest(getManifest(), getMainClassName(), CLASSES_FOLDER, LIB_FOLDER, CLASSPATH_INDEX,
				(this.layered != null) ? LAYERS_INDEX : null);
		super.copy();
	}

	@Override
	protected CopyAction createCopyAction() {
		if (this.layered != null) {
			LayerResolver layerResolver = new LayerResolver(getConfigurations(), this.layered, this::isLibrary);
			String layerToolsLocation = this.layered.isIncludeLayerTools() ? LIB_FOLDER : null;
			return this.support.createCopyAction(this, layerResolver, layerToolsLocation);
		}
		return this.support.createCopyAction(this);
	}

	@Internal
	protected Iterable<Configuration> getConfigurations() {
		return getProject().getConfigurations();
	}

	@Override
	public String getMainClassName() {
		if (this.mainClassName == null) {
			String manifestStartClass = (String) getManifest().getAttributes().get("Start-Class");
			if (manifestStartClass != null) {
				setMainClassName(manifestStartClass);
			}
		}
		return this.mainClassName;
	}

	@Override
	public void setMainClassName(String mainClassName) {
		this.mainClassName = mainClassName;
	}

	@Override
	public void requiresUnpack(String... patterns) {
		this.support.requiresUnpack(patterns);
	}

	@Override
	public void requiresUnpack(Spec<FileTreeElement> spec) {
		this.support.requiresUnpack(spec);
	}

	@Override
	public LaunchScriptConfiguration getLaunchScript() {
		return this.support.getLaunchScript();
	}

	@Override
	public void launchScript() {
		enableLaunchScriptIfNecessary();
	}

	@Override
	public void launchScript(Action<LaunchScriptConfiguration> action) {
		action.execute(enableLaunchScriptIfNecessary());
	}

	@Nested
	@Optional
	public LayeredSpec getLayered() {
		return this.layered;
	}

	public void layered() {
		layered(true);
	}

	public void layered(boolean layered) {
		this.layered = layered ? new LayeredSpec() : null;
	}

	public void layered(Closure<?> closure) {
		layered(ConfigureUtil.configureUsing(closure));
	}

	public void layered(Action<LayeredSpec> action) {
		LayeredSpec layered = new LayeredSpec();
		action.execute(layered);
		this.layered = layered;
	}

	@Override
	public FileCollection getClasspath() {
		return this.classpath;
	}

	@Override
	public void classpath(Object... classpath) {
		FileCollection existingClasspath = this.classpath;
		this.classpath = getProject().files((existingClasspath != null) ? existingClasspath : Collections.emptyList(),
				classpath);
	}

	@Override
	public void setClasspath(Object classpath) {
		this.classpath = getProject().files(classpath);
	}

	@Override
	public void setClasspath(FileCollection classpath) {
		this.classpath = getProject().files(classpath);
	}

	@Override
	public boolean isExcludeDevtools() {
		return this.support.isExcludeDevtools();
	}

	@Override
	public void setExcludeDevtools(boolean excludeDevtools) {
		this.support.setExcludeDevtools(excludeDevtools);
	}

	/**
	 * Returns a {@code CopySpec} that can be used to add content to the {@code BOOT-INF}
	 * directory of the jar.
	 * @return a {@code CopySpec} for {@code BOOT-INF}
	 * @since 2.0.3
	 */
	@Internal
	public CopySpec getBootInf() {
		CopySpec child = getProject().copySpec();
		this.bootInfSpec.with(child);
		return child;
	}

	/**
	 * Calls the given {@code action} to add content to the {@code BOOT-INF} directory of
	 * the jar.
	 * @param action the {@code Action} to call
	 * @return the {@code CopySpec} for {@code BOOT-INF} that was passed to the
	 * {@code Action}
	 * @since 2.0.3
	 */
	public CopySpec bootInf(Action<CopySpec> action) {
		CopySpec bootInf = getBootInf();
		action.execute(bootInf);
		return bootInf;
	}

	/**
	 * Return the {@link ZipCompression} that should be used when adding the file
	 * represented by the given {@code details} to the jar. By default, any
	 * {@link #isLibrary(FileCopyDetails) library} is {@link ZipCompression#STORED stored}
	 * and all other files are {@link ZipCompression#DEFLATED deflated}.
	 * @param details the file copy details
	 * @return the compression to use
	 */
	protected ZipCompression resolveZipCompression(FileCopyDetails details) {
		return isLibrary(details) ? ZipCompression.STORED : ZipCompression.DEFLATED;
	}

	/**
	 * Return if the {@link FileCopyDetails} are for a library. By default any file in
	 * {@code BOOT-INF/lib} is considered to be a library.
	 * @param details the file copy details
	 * @return {@code true} if the details are for a library
	 */
	protected boolean isLibrary(FileCopyDetails details) {
		String path = details.getRelativePath().getPathString();
		return path.startsWith(LIB_FOLDER);
	}

	private LaunchScriptConfiguration enableLaunchScriptIfNecessary() {
		LaunchScriptConfiguration launchScript = this.support.getLaunchScript();
		if (launchScript == null) {
			launchScript = new LaunchScriptConfiguration(this);
			this.support.setLaunchScript(launchScript);
		}
		return launchScript;
	}

	/**
	 * Syntactic sugar that makes {@link CopySpec#into} calls a little easier to read.
	 * @param <T> the result type
	 * @param callable the callable
	 * @return an action to add the callable to the spec
	 */
	private static <T> Action<CopySpec> fromCallTo(Callable<T> callable) {
		return (spec) -> spec.from(callTo(callable));
	}

	/**
	 * Syntactic sugar that makes {@link CopySpec#from} calls a little easier to read.
	 * @param <T> the result type
	 * @param callable the callable
	 * @return the callable
	 */
	private static <T> Callable<T> callTo(Callable<T> callable) {
		return callable;
	}

}
