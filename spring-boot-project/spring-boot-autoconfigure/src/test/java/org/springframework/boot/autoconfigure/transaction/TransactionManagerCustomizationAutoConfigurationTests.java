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

package org.springframework.boot.autoconfigure.transaction;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TransactionManagerCustomizationAutoConfiguration}.
 *
 * @author Andy Wilkinson
 */
class TransactionManagerCustomizationAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(TransactionManagerCustomizationAutoConfiguration.class));

	@Test
	void autoConfiguresTransactionManagerCustomizers() {
		this.contextRunner.run((context) -> {
			TransactionManagerCustomizers customizers = context.getBean(TransactionManagerCustomizers.class);
			assertThat(customizers).extracting("customizers")
				.asList()
				.hasSize(2)
				.hasAtLeastOneElementOfType(TransactionProperties.class)
				.hasAtLeastOneElementOfType(ExecutionListenersTransactionManagerCustomizer.class);
		});
	}

	@Test
	void autoConfiguredTransactionManagerCustomizersBacksOff() {
		this.contextRunner.withUserConfiguration(CustomTransactionManagerCustomizersConfiguration.class)
			.run((context) -> {
				TransactionManagerCustomizers customizers = context.getBean(TransactionManagerCustomizers.class);
				assertThat(customizers).extracting("customizers").asList().isEmpty();
			});
	}

	@Configuration(proxyBeanMethods = false)
	static class CustomTransactionManagerCustomizersConfiguration {

		@Bean
		TransactionManagerCustomizers customTransactionManagerCustomizers() {
			return TransactionManagerCustomizers.of(Collections.<TransactionManagerCustomizer<?>>emptyList());
		}

	}

}
