/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;

import org.springframework.boot.loader.tools.DefaultLaunchScript;
import org.springframework.boot.loader.tools.LaunchScript;
import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;
import org.springframework.boot.loader.tools.Layouts.Expanded;
import org.springframework.boot.loader.tools.Layouts.Jar;
import org.springframework.boot.loader.tools.Layouts.None;
import org.springframework.boot.loader.tools.Layouts.War;
import org.springframework.boot.loader.tools.Libraries;
import org.springframework.boot.loader.tools.Repackager;
import org.springframework.boot.loader.tools.Repackager.MainClassTimeoutWarningListener;

/**
 * Repackages existing JAR and WAR archives so that they can be executed from the command
 * line using {@literal java -jar}. With <code>layout=NONE</code> can also be used simply
 * to package a JAR with nested dependencies (and no main class, so not executable).
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Björn Lindström
 */
@Mojo(name = "repackage", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RepackageMojo extends AbstractDependencyFilterMojo {

	private static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s+");

	/**
	 * The Maven project.
	 * @since 1.0
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	/**
	 * Maven project helper utils.
	 * @since 1.0
	 */
	@Component
	private MavenProjectHelper projectHelper;

	/**
	 * Directory containing the generated archive.
	 * @since 1.0
	 */
	@Parameter(defaultValue = "${project.build.directory}", required = true)
	private File outputDirectory;

	/**
	 * Name of the generated archive.
	 * @since 1.0
	 */
	@Parameter(defaultValue = "${project.build.finalName}", readonly = true)
	private String finalName;

	/**
	 * Skip the execution.
	 * @since 1.2
	 */
	@Parameter(property = "spring-boot.repackage.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Classifier to add to the repackaged archive. If not given, the main artifact will
	 * be replaced by the repackaged archive. If given, the classifier will also be used
	 * to determine the source archive to repackage: if an artifact with that classifier
	 * already exists, it will be used as source and replaced. If no such artifact exists,
	 * the main artifact will be used as source and the repackaged archive will be
	 * attached as a supplemental artifact with that classifier. Attaching the artifact
	 * allows to deploy it alongside to the original one, see <a href=
	 * "https://maven.apache.org/plugins/maven-deploy-plugin/examples/deploying-with-classifiers.html"
	 * > the maven documentation for more details</a>.
	 * @since 1.0
	 */
	@Parameter
	private String classifier;

	/**
	 * Attach the repackaged archive to be installed and deployed.
	 * @since 1.4
	 */
	@Parameter(defaultValue = "true")
	private boolean attach = true;

	/**
	 * The name of the main class. If not specified the first compiled class found that
	 * contains a 'main' method will be used.
	 * @since 1.0
	 */
	@Parameter
	private String mainClass;

	/**
	 * The type of archive (which corresponds to how the dependencies are laid out inside
	 * it). Possible values are JAR, WAR, ZIP, DIR, NONE. Defaults to a guess based on the
	 * archive type.
	 * @since 1.0
	 */
	@Parameter
	private LayoutType layout;

	/**
	 * The layout factory that will be used to create the executable archive if no
	 * explicit layout is set. Alternative layouts implementations can be provided by 3rd
	 * parties.
	 * @since 1.5
	 */
	@Parameter
	private LayoutFactory layoutFactory;

	/**
	 * A list of the libraries that must be unpacked from fat jars in order to run.
	 * Specify each library as a {@code <dependency>} with a {@code <groupId>} and a
	 * {@code <artifactId>} and they will be unpacked at runtime.
	 * @since 1.1
	 */
	@Parameter
	private List<Dependency> requiresUnpack;

	/**
	 * Make a fully executable jar for *nix machines by prepending a launch script to the
	 * jar.
	 * <p>
	 * Currently, some tools do not accept this format so you may not always be able to
	 * use this technique. For example, {@code jar -xf} may silently fail to extract a jar
	 * or war that has been made fully-executable. It is recommended that you only enable
	 * this option if you intend to execute it directly, rather than running it with
	 * {@code java -jar} or deploying it to a servlet container.
	 * @since 1.3
	 */
	@Parameter(defaultValue = "false")
	private boolean executable;

	/**
	 * The embedded launch script to prepend to the front of the jar if it is fully
	 * executable. If not specified the 'Spring Boot' default script will be used.
	 * @since 1.3
	 */
	@Parameter
	private File embeddedLaunchScript;

	/**
	 * Properties that should be expanded in the embedded launch script.
	 * @since 1.3
	 */
	@Parameter
	private Properties embeddedLaunchScriptProperties;

	/**
	 * Exclude Spring Boot devtools from the repackaged archive.
	 * @since 1.3
	 */
	@Parameter(defaultValue = "true")
	private boolean excludeDevtools = true;

	/**
	 * Include system scoped dependencies.
	 * @since 1.4
	 */
	@Parameter(defaultValue = "false")
	public boolean includeSystemScope;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.project.getPackaging().equals("pom")) {
			getLog().debug("repackage goal could not be applied to pom project.");
			return;
		}
		if (this.skip) {
			getLog().debug("skipping repackaging as per configuration.");
			return;
		}
		repackage();
	}

	private void repackage() throws MojoExecutionException {
		Artifact source = getSourceArtifact();
		File target = getTargetFile();
		Repackager repackager = getRepackager(source.getFile());
		Set<Artifact> artifacts = filterDependencies(this.project.getArtifacts(),
				getFilters(getAdditionalFilters()));
		Libraries libraries = new ArtifactsLibraries(artifacts, this.requiresUnpack,
				getLog());
		try {
			LaunchScript launchScript = getLaunchScript();
			repackager.repackage(target, libraries, launchScript);
		}
		catch (IOException ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
		updateArtifact(source, target, repackager.getBackupFile());
	}

	/**
	 * Return the source {@link Artifact} to repackage. If a classifier is specified and
	 * an artifact with that classifier exists, it is used. Otherwise, the main artifact
	 * is used.
	 * @return the source artifact to repackage
	 */
	private Artifact getSourceArtifact() {
		Artifact sourceArtifact = getArtifact(this.classifier);
		return (sourceArtifact != null) ? sourceArtifact : this.project.getArtifact();
	}

	private Artifact getArtifact(String classifier) {
		if (classifier != null) {
			for (Artifact attachedArtifact : this.project.getAttachedArtifacts()) {
				if (classifier.equals(attachedArtifact.getClassifier())
						&& attachedArtifact.getFile() != null
						&& attachedArtifact.getFile().isFile()) {
					return attachedArtifact;
				}
			}
		}
		return null;
	}

	private File getTargetFile() {
		String classifier = (this.classifier != null) ? this.classifier.trim() : "";
		if (!classifier.isEmpty() && !classifier.startsWith("-")) {
			classifier = "-" + classifier;
		}
		if (!this.outputDirectory.exists()) {
			this.outputDirectory.mkdirs();
		}
		String finalName = this.project.getBuild().getFinalName();
		return new File(this.outputDirectory, finalName + classifier + "."
				+ this.project.getArtifact().getArtifactHandler().getExtension());
	}

	private Repackager getRepackager(File source) {
		Repackager repackager = new Repackager(source, this.layoutFactory);
		repackager.addMainClassTimeoutWarningListener(
				new LoggingMainClassTimeoutWarningListener());
		repackager.setMainClass(this.mainClass);
		if (this.layout != null) {
			getLog().info("Layout: " + this.layout);
			repackager.setLayout(this.layout.layout());
		}
		return repackager;
	}

	private ArtifactsFilter[] getAdditionalFilters() {
		List<ArtifactsFilter> filters = new ArrayList<>();
		if (this.excludeDevtools) {
			Exclude exclude = new Exclude();
			exclude.setGroupId("org.springframework.boot");
			exclude.setArtifactId("spring-boot-devtools");
			ExcludeFilter filter = new ExcludeFilter(exclude);
			filters.add(filter);
		}
		if (!this.includeSystemScope) {
			filters.add(new ScopeFilter(null, Artifact.SCOPE_SYSTEM));
		}
		return filters.toArray(new ArtifactsFilter[0]);
	}

	private LaunchScript getLaunchScript() throws IOException {
		if (this.executable || this.embeddedLaunchScript != null) {
			return new DefaultLaunchScript(this.embeddedLaunchScript,
					buildLaunchScriptProperties());
		}
		return null;
	}

	private Properties buildLaunchScriptProperties() {
		Properties properties = new Properties();
		if (this.embeddedLaunchScriptProperties != null) {
			properties.putAll(this.embeddedLaunchScriptProperties);
		}
		putIfMissing(properties, "initInfoProvides", this.project.getArtifactId());
		putIfMissing(properties, "initInfoShortDescription", this.project.getName(),
				this.project.getArtifactId());
		putIfMissing(properties, "initInfoDescription",
				removeLineBreaks(this.project.getDescription()), this.project.getName(),
				this.project.getArtifactId());
		return properties;
	}

	private String removeLineBreaks(String description) {
		return (description != null)
				? WHITE_SPACE_PATTERN.matcher(description).replaceAll(" ") : null;
	}

	private void putIfMissing(Properties properties, String key,
			String... valueCandidates) {
		if (!properties.containsKey(key)) {
			for (String candidate : valueCandidates) {
				if (candidate != null && !candidate.isEmpty()) {
					properties.put(key, candidate);
					return;
				}
			}
		}
	}

	private void updateArtifact(Artifact source, File target, File original) {
		if (this.attach) {
			attachArtifact(source, target);
		}
		else if (source.getFile().equals(target) && original.exists()) {
			String artifactId = (this.classifier != null)
					? "artifact with classifier " + this.classifier : "main artifact";
			getLog().info(String.format("Updating %s %s to %s", artifactId,
					source.getFile(), original));
			source.setFile(original);
		}
		else if (this.classifier != null) {
			getLog().info("Creating repackaged archive " + target + " with classifier "
					+ this.classifier);
		}
	}

	private void attachArtifact(Artifact source, File target) {
		if (this.classifier != null && !source.getFile().equals(target)) {
			getLog().info("Attaching repackaged archive " + target + " with classifier "
					+ this.classifier);
			this.projectHelper.attachArtifact(this.project, this.project.getPackaging(),
					this.classifier, target);
		}
		else {
			String artifactId = (this.classifier != null)
					? "artifact with classifier " + this.classifier : "main artifact";
			getLog().info("Replacing " + artifactId + " with repackaged archive");
			source.setFile(target);
		}
	}

	private class LoggingMainClassTimeoutWarningListener
			implements MainClassTimeoutWarningListener {

		@Override
		public void handleTimeoutWarning(long duration, String mainMethod) {
			getLog().warn("Searching for the main-class is taking some time, "
					+ "consider using the mainClass configuration " + "parameter");
		}

	}

	/**
	 * Archive layout types.
	 */
	public enum LayoutType {

		/**
		 * Jar Layout.
		 */
		JAR(new Jar()),

		/**
		 * War Layout.
		 */
		WAR(new War()),

		/**
		 * Zip Layout.
		 */
		ZIP(new Expanded()),

		/**
		 * Dir Layout.
		 */
		DIR(new Expanded()),

		/**
		 * No Layout.
		 */
		NONE(new None());

		private final Layout layout;

		LayoutType(Layout layout) {
			this.layout = layout;
		}

		public Layout layout() {
			return this.layout;
		}

	}

}
