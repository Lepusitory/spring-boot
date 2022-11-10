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

package org.springframework.boot.actuate.autoconfigure.endpoint.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.endpoint.jackson.EndpointObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JacksonEndpointAutoConfiguration}.
 *
 * @author Phillip Webb
 */
class JacksonEndpointAutoConfigurationTests {

	private ApplicationContextRunner runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(JacksonEndpointAutoConfiguration.class));

	@Test
	void endpointObjectMapperWhenNoProperty() {
		this.runner.run((context) -> assertThat(context).hasSingleBean(EndpointObjectMapper.class));
	}

	@Test
	void endpointObjectMapperWhenPropertyTrue() {
		this.runner.withPropertyValues("management.endpoints.jackson.isolated-object-mapper=true")
				.run((context) -> assertThat(context).hasSingleBean(EndpointObjectMapper.class));
	}

	@Test
	void endpointObjectMapperWhenPropertyFalse() {
		this.runner.withPropertyValues("management.endpoints.jackson.isolated-object-mapper=false")
				.run((context) -> assertThat(context).doesNotHaveBean(EndpointObjectMapper.class));
	}

	@Configuration(proxyBeanMethods = false)
	static class TestEndpointMapperConfiguration {

		@Bean
		TestEndpointObjectMapper testEndpointObjectMapper() {
			return new TestEndpointObjectMapper();
		}

	}

	static class TestEndpointObjectMapper implements EndpointObjectMapper {

		@Override
		public ObjectMapper get() {
			return null;
		}

	}

}
