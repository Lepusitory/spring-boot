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

package org.springframework.boot.test.autoconfigure.orm.jpa;

import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.test.autoconfigure.filter.StandardAnnotationCustomizableTypeExcludeFilter;

/**
 * {@link TypeExcludeFilter} for {@link DataJpaTest @DataJpaTest}.
 *
 * @author Phillip Webb
 */
class DataJpaTypeExcludeFilter extends StandardAnnotationCustomizableTypeExcludeFilter<DataJpaTest> {

	DataJpaTypeExcludeFilter(Class<?> testClass) {
		super(testClass);
	}

}
