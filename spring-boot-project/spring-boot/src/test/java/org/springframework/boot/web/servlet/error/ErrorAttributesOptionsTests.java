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

package org.springframework.boot.web.servlet.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;

/**
 * Tests for {@link ErrorAttributesOptions}.
 *
 * @author Wanderlei Souza
 */
class ErrorAttributesOptionsTests {

	@Test
	void excludingFromEmptySetWithoutErrors() {
		Set<Include> includes = EnumSet.noneOf(Include.class);
		ErrorAttributeOptions errorAttributeOptions = ErrorAttributeOptions.of(includes);
		errorAttributeOptions = errorAttributeOptions.excluding(Include.EXCEPTION);
		assertThat(errorAttributeOptions.getIncludes().isEmpty());
	}
	
	@Test
	void includingInEmptySetWithoutErrors() {
		Set<Include> includes = EnumSet.noneOf(Include.class);
		ErrorAttributeOptions errorAttributeOptions = ErrorAttributeOptions.of(includes);
		errorAttributeOptions = errorAttributeOptions.including(Include.EXCEPTION);
		assertThat(errorAttributeOptions.getIncludes().isEmpty());
	}

}
