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

package org.springframework.boot.devtools.tests;

import java.io.File;

import org.junit.rules.TemporaryFolder;

import org.springframework.boot.testsupport.BuildOutput;

/**
 * Various directories used by the {@link ApplicationLauncher ApplicationLaunchers}.
 *
 * @author Andy Wilkinson
 */
class Directories {

	private final BuildOutput buildOutput;

	private final TemporaryFolder temp;

	Directories(BuildOutput buildOutput, TemporaryFolder temp) {
		this.buildOutput = buildOutput;
		this.temp = temp;
	}

	File getTestClassesDirectory() {
		return this.buildOutput.getTestClassesLocation();
	}

	File getRemoteAppDirectory() {
		return new File(this.temp.getRoot(), "remote");
	}

	File getDependenciesDirectory() {
		return new File(this.buildOutput.getRootLocation(), "dependencies");
	}

	File getAppDirectory() {
		return new File(this.temp.getRoot(), "app");
	}

}
