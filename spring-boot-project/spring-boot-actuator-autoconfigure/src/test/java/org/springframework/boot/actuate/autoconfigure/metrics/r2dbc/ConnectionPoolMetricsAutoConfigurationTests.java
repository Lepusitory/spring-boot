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

package org.springframework.boot.actuate.autoconfigure.metrics.r2dbc;

import java.util.Collections;
import java.util.UUID;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.r2dbc.h2.CloseableConnectionFactory;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionOption;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.autoconfigure.metrics.test.MetricsRun;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConnectionPoolMetricsAutoConfiguration}.
 *
 * @author Tadaya Tsuyukubo
 * @author Stephane Nicoll
 */
class ConnectionPoolMetricsAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withPropertyValues("spring.r2dbc.generate-unique-name=true").with(MetricsRun.simple())
			.withConfiguration(AutoConfigurations.of(ConnectionPoolMetricsAutoConfiguration.class))
			.withUserConfiguration(BaseConfiguration.class);

	@Test
	void autoConfiguredDataSourceIsInstrumented() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(R2dbcAutoConfiguration.class)).run((context) -> {
			MeterRegistry registry = context.getBean(MeterRegistry.class);
			assertThat(registry.find("r2dbc.pool.acquired").gauges()).hasSize(1);
		});
	}

	@Test
	void connectionPoolInstrumentationCanBeDisabled() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(R2dbcAutoConfiguration.class))
				.withPropertyValues("management.metrics.enable.r2dbc=false").run((context) -> {
					MeterRegistry registry = context.getBean(MeterRegistry.class);
					assertThat(registry.find("r2dbc.pool.acquired").gauge()).isNull();
				});
	}

	@Test
	void allConnectionPoolsCanBeInstrumented() {
		this.contextRunner.withUserConfiguration(TwoConnectionPoolsConfiguration.class).run((context) -> {
			MeterRegistry registry = context.getBean(MeterRegistry.class);
			assertThat(registry.find("r2dbc.pool.acquired").gauges()).extracting(Meter::getId)
					.extracting((id) -> id.getTag("name")).containsExactlyInAnyOrder("firstPool", "secondPool");
		});
	}

	@Configuration(proxyBeanMethods = false)
	static class BaseConfiguration {

		@Bean
		SimpleMeterRegistry registry() {
			return new SimpleMeterRegistry();
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class TwoConnectionPoolsConfiguration {

		@Bean
		CloseableConnectionFactory connectionFactory() {
			return H2ConnectionFactory.inMemory("db-" + UUID.randomUUID(), "sa", "",
					Collections.singletonMap(H2ConnectionOption.DB_CLOSE_DELAY, "-1"));
		}

		@Bean
		ConnectionPool firstPool(ConnectionFactory connectionFactory) {
			return new ConnectionPool(ConnectionPoolConfiguration.builder(connectionFactory).build());
		}

		@Bean
		ConnectionPool secondPool(ConnectionFactory connectionFactory) {
			return new ConnectionPool(ConnectionPoolConfiguration.builder(connectionFactory).build());
		}

	}

}
