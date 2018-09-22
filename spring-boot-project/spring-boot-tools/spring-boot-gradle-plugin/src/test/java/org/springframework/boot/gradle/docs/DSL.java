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

/**
 * The DSLs supported by Gradle and demonstrated in the documentation samples
 */
public enum DSL {

	GROOVY("Groovy", ".gradle"), KOTLIN("Kotlin", ".gradle.kts");

	private final String name;

	private final String extension;

	DSL(String name, String extension) {
		this.name = name;
		this.extension = extension;
	}

	/**
	 * Gets the user-friendly name of the DSL
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the file extension of build scripts (starting with a dot)
	 */
	public String getExtension() {
		return this.extension;
	}

}
