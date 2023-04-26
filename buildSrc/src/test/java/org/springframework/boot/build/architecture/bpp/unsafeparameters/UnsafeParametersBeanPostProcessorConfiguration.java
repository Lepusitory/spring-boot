/*
 * Copyright 2012-2023 the original author or authors.
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

package org.springframework.boot.build.architecture.bpp.unsafeparameters;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

class UnsafeParametersBeanPostProcessorConfiguration {

	@Bean
	static BeanPostProcessor unsafeParametersBeanPostProcessor(ApplicationContext context, Integer beanOne,
			String beanTwo) {
		return new BeanPostProcessor() {

		};
	}

	@Bean
	Integer beanOne() {
		return 1;
	}

	@Bean
	String beanTwo() {
		return "test";
	}

}
