/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.gradle.docs;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.gradle.testkit.GradleBuild;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the managing dependencies documentation.
 *
 * @author Andy Wilkinson
 * @author Jean-Baptiste Nizet
 */
@RunWith(GradleMultiDslSuite.class)
public class ManagingDependenciesDocumentationTests {

	@Rule
	public GradleBuild gradleBuild;

	public DSL dsl;

	@Test
	public void dependenciesExampleEvaluatesSuccessfully() {
		this.gradleBuild.script("src/main/gradle/managing-dependencies/dependencies"
				+ this.dsl.getExtension()).build();
	}

	@Test
	public void customManagedVersions() {
		assertThat(
				this.gradleBuild
						.script("src/main/gradle/managing-dependencies/custom-version"
								+ this.dsl.getExtension())
						.build("slf4jVersion").getOutput()).contains("1.7.20");
	}

	@Test
	public void dependencyManagementInIsolation() {
		assertThat(this.gradleBuild
				.script("src/main/gradle/managing-dependencies/configure-bom"
						+ this.dsl.getExtension())
				.build("dependencyManagement").getOutput())
						.contains("org.springframework.boot:spring-boot-starter ");
	}

	@Test
	public void dependencyManagementInIsolationWithPluginsBlock() {
		if (this.dsl == DSL.KOTLIN) {
			assertThat(this.gradleBuild.script(
					"src/main/gradle/managing-dependencies/configure-bom-with-plugins"
							+ this.dsl.getExtension())
					.build("dependencyManagement").getOutput())
							.contains("org.springframework.boot:spring-boot-starter ");
		}
	}

}
