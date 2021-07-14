/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.boot.gradle.plugin;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.boot.gradle.junit.GradleProjectBuilder;
import org.springframework.boot.testsupport.classpath.ClassPathExclusions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringBootPlugin}.
 *
 * @author Martin Chalupa
 * @author Andy Wilkinson
 */
@ClassPathExclusions("kotlin-daemon-client-*.jar")
class SpringBootPluginTests {

	@TempDir
	File temp;

	@Test
	void bootArchivesConfigurationsCannotBeResolved() {
		Project project = GradleProjectBuilder.builder().withProjectDir(this.temp).build();
		project.getPlugins().apply(SpringBootPlugin.class);
		Configuration bootArchives = project.getConfigurations()
				.getByName(SpringBootPlugin.BOOT_ARCHIVES_CONFIGURATION_NAME);
		assertThat(bootArchives.isCanBeResolved()).isFalse();
	}

}
