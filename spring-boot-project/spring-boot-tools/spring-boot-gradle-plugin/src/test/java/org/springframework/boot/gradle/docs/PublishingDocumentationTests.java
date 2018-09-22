/*
 * Copyright 2012-2017 the original author or authors.
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

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.gradle.testkit.GradleBuild;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the publishing documentation.
 *
 * @author Andy Wilkinson
 * @author Jean-Baptiste Nizet
 */
@RunWith(GradleMultiDslSuite.class)
public class PublishingDocumentationTests {

	@Rule
	public GradleBuild gradleBuild;

	public DSL dsl;

	@Test
	public void mavenUpload() throws IOException {
		assertThat(this.gradleBuild
				.script("src/main/gradle/publishing/maven" + this.dsl.getExtension())
				.build("deployerRepository").getOutput())
						.contains("https://repo.example.com");
	}

	@Test
	public void mavenPublish() throws IOException {
		assertThat(this.gradleBuild
				.script("src/main/gradle/publishing/maven-publish"
						+ this.dsl.getExtension())
				.build("publishingConfiguration").getOutput())
						.contains("MavenPublication")
						.contains("https://repo.example.com");
	}

}
