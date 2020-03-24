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

package org.springframework.boot.autoconfigure.data.cassandra;

import java.util.Set;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.TestAutoConfigurationPackage;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.data.alt.cassandra.ReactiveCityCassandraRepository;
import org.springframework.boot.autoconfigure.data.cassandra.city.City;
import org.springframework.boot.autoconfigure.data.cassandra.city.ReactiveCityRepository;
import org.springframework.boot.autoconfigure.data.empty.EmptyDataPackage;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link CassandraReactiveRepositoriesAutoConfiguration}.
 *
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @author Mark Paluch
 * @author Andy Wilkinson
 */
class CassandraReactiveRepositoriesAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
			AutoConfigurations.of(CassandraAutoConfiguration.class, CassandraRepositoriesAutoConfiguration.class,
					CassandraDataAutoConfiguration.class, CassandraReactiveDataAutoConfiguration.class,
					CassandraReactiveRepositoriesAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class));

	@Test
	void testDefaultRepositoryConfiguration() {
		this.contextRunner.withUserConfiguration(DefaultConfiguration.class).run((context) -> {
			assertThat(context).hasSingleBean(ReactiveCityRepository.class);
			assertThat(context).hasSingleBean(CqlSessionBuilder.class);
			assertThat(getInitialEntitySet(context)).hasSize(1);
		});
	}

	@Test
	void testNoRepositoryConfiguration() {
		this.contextRunner.withUserConfiguration(EmptyConfiguration.class).run((context) -> {
			assertThat(context).hasSingleBean(CqlSessionBuilder.class);
			assertThat(getInitialEntitySet(context)).isEmpty();
		});
	}

	@Test
	void doesNotTriggerDefaultRepositoryDetectionIfCustomized() {
		this.contextRunner.withUserConfiguration(CustomizedConfiguration.class).run((context) -> {
			assertThat(context).hasSingleBean(ReactiveCityCassandraRepository.class);
			assertThat(getInitialEntitySet(context)).hasSize(1).containsOnly(City.class);
		});
	}

	@Test
	void enablingImperativeRepositoriesDisablesReactiveRepositories() {
		this.contextRunner.withUserConfiguration(DefaultConfiguration.class)
				.withPropertyValues("spring.data.cassandra.repositories.type=imperative")
				.run((context) -> assertThat(context).doesNotHaveBean(ReactiveCityRepository.class));
	}

	@Test
	void enablingNoRepositoriesDisablesReactiveRepositories() {
		this.contextRunner.withUserConfiguration(DefaultConfiguration.class)
				.withPropertyValues("spring.data.cassandra.repositories.type=none")
				.run((context) -> assertThat(context).doesNotHaveBean(ReactiveCityRepository.class));
	}

	@SuppressWarnings("unchecked")
	private Set<Class<?>> getInitialEntitySet(ApplicationContext context) {
		CassandraMappingContext mappingContext = context.getBean(CassandraMappingContext.class);
		return (Set<Class<?>>) ReflectionTestUtils.getField(mappingContext, "initialEntitySet");
	}

	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

		@Bean
		CqlSession cqlSession() {
			CodecRegistry codecRegistry = mock(CodecRegistry.class);
			DriverContext context = mock(DriverContext.class);
			CqlSession cqlSession = mock(CqlSession.class);
			when(context.getCodecRegistry()).thenReturn(codecRegistry);
			when(cqlSession.getContext()).thenReturn(context);
			return cqlSession;
		}

	}

	@Configuration(proxyBeanMethods = false)
	@TestAutoConfigurationPackage(EmptyDataPackage.class)
	@Import(TestConfiguration.class)
	static class EmptyConfiguration {

	}

	@Configuration(proxyBeanMethods = false)
	@TestAutoConfigurationPackage(City.class)
	@Import(TestConfiguration.class)
	static class DefaultConfiguration {

	}

	@Configuration(proxyBeanMethods = false)
	@TestAutoConfigurationPackage(CassandraReactiveRepositoriesAutoConfigurationTests.class)
	@EnableReactiveCassandraRepositories(basePackageClasses = ReactiveCityCassandraRepository.class)
	@Import(TestConfiguration.class)
	static class CustomizedConfiguration {

	}

}
