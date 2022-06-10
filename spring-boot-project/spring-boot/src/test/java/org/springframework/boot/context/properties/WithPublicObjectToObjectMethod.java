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

package org.springframework.boot.context.properties;

import java.util.Optional;

/**
 * Data object with a pubic method picked up by the {@code ObjectToObjectConverter}. Used
 * in {@link ConfigurationPropertiesTests}.
 *
 * @author Phillip Webb
 */
public class WithPublicObjectToObjectMethod {

	private final String value;

	WithPublicObjectToObjectMethod(String value) {
		this.value = value;
	}

	String getValue() {
		return this.value;
	}

	public static Optional<WithPublicObjectToObjectMethod> from(String value) {
		return Optional.of(new WithPublicObjectToObjectMethod(value));
	}

}
