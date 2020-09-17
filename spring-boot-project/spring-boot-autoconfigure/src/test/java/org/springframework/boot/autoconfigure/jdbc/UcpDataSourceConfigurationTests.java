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

package org.springframework.boot.autoconfigure.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceImpl;

/**
 * Tests for {@link DataSourceAutoConfiguration} with Oracle UCP.
 *
 * @author Fabio Grassi
 */
class UcpDataSourceConfigurationTests {

	private static final String PREFIX = "spring.datasource.ucp.";

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class))
			.withPropertyValues("spring.datasource.initialization-mode=never",
					"spring.datasource.type=" + PoolDataSource.class.getName());

	@Test
	void testDataSourceExists() {
		this.contextRunner.run((context) -> {
			assertThat(context.getBeansOfType(DataSource.class)).hasSize(1);
			assertThat(context.getBeansOfType(PoolDataSourceImpl.class)).hasSize(1);
		});
	}

	@Test
	void testDataSourcePropertiesOverridden() {
		this.contextRunner.withPropertyValues(PREFIX + "URL=jdbc:foo//bar/spam", PREFIX + "maxIdleTime=1234")
				.run((context) -> {
					PoolDataSourceImpl ds = context.getBean(PoolDataSourceImpl.class);
					assertThat(ds.getURL()).isEqualTo("jdbc:foo//bar/spam");
					assertThat(ds.getMaxIdleTime()).isEqualTo(1234);
				});
	}

	@Test
	void testDataSourceConnectionPropertiesOverridden() {
		this.contextRunner.withPropertyValues(PREFIX + "connectionProperties.autoCommit=false").run((context) -> {
			PoolDataSourceImpl ds = context.getBean(PoolDataSourceImpl.class);
			assertThat(ds.getConnectionProperty("autoCommit")).isEqualTo("false");

		});
	}

	@Test
	void testDataSourceDefaultsPreserved() {
		this.contextRunner.run((context) -> {
			PoolDataSourceImpl ds = context.getBean(PoolDataSourceImpl.class);
			assertThat(ds.getInitialPoolSize()).isEqualTo(0);
			assertThat(ds.getMinPoolSize()).isEqualTo(0);
			assertThat(ds.getMaxPoolSize()).isEqualTo(Integer.MAX_VALUE);
			assertThat(ds.getInactiveConnectionTimeout()).isEqualTo(0);
			assertThat(ds.getConnectionWaitTimeout()).isEqualTo(3);
			assertThat(ds.getTimeToLiveConnectionTimeout()).isEqualTo(0);
			assertThat(ds.getAbandonedConnectionTimeout()).isEqualTo(0);
			assertThat(ds.getTimeoutCheckInterval()).isEqualTo(30);
			assertThat(ds.getFastConnectionFailoverEnabled()).isFalse();
		});
	}

	@Test
	void nameIsAliasedToPoolName() {
		this.contextRunner.withPropertyValues("spring.datasource.name=myDS").run((context) -> {
			PoolDataSourceImpl ds = context.getBean(PoolDataSourceImpl.class);
			assertThat(ds.getConnectionPoolName()).isEqualTo("myDS");

		});
	}

	@Test
	void connectionPoolNameTakesPrecedenceOverName() {
		this.contextRunner.withPropertyValues("spring.datasource.name=myDS", PREFIX + "connectionPoolName=myUcpDS")
				.run((context) -> {
					PoolDataSourceImpl ds = context.getBean(PoolDataSourceImpl.class);
					assertThat(ds.getConnectionPoolName()).isEqualTo("myUcpDS");
				});
	}

}
