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

package org.springframework.boot.test.context.filter;

import org.junit.runner.RunWith;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Abstract test with nest {@code @Configuration} and {@code @RunWith} used by
 * {@link TestTypeExcludeFilter}.
 *
 * @author Phillip Webb
 */
@RunWith(SpringRunner.class)
public abstract class AbstractTestWithConfigAndRunWith {

	@Configuration
	static class Config {

	}

}
