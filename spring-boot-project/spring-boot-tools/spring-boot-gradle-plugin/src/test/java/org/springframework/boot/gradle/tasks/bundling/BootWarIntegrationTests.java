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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.boot.gradle.junit.GradleCompatibility;

/**
 * Integration tests for {@link BootWar}.
 *
 * @author Andy Wilkinson
 */
@GradleCompatibility(configurationCache = true)
class BootWarIntegrationTests extends AbstractBootArchiveIntegrationTests {

	BootWarIntegrationTests() {
		super("bootWar", "WEB-INF/lib/", "WEB-INF/classes/", "WEB-INF/");
	}

	@Override
	String[] getExpectedApplicationLayerContents(String... additionalFiles) {
		Set<String> contents = new TreeSet<>(Arrays.asList(additionalFiles));
		contents.addAll(Arrays.asList("WEB-INF/layers.idx", "META-INF/"));
		return contents.toArray(new String[0]);
	}

}
