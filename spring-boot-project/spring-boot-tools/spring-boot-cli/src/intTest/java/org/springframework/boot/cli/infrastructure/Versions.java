/*
 * Copyright 2012-2022 the original author or authors.
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

package org.springframework.boot.cli.infrastructure;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Provides access to the current Boot version by referring to {@code gradle.properties}.
 *
 * @author Andy Wilkinson
 */
final class Versions {

	private Versions() {
	}

	static String getBootVersion() {
		Properties gradleProperties = new Properties();
		try (FileInputStream input = new FileInputStream("../../../gradle.properties")) {
			gradleProperties.load(input);
			return gradleProperties.getProperty("version");
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
