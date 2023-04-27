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

package org.springframework.boot.docker.compose.service.connection.oracle;

import java.sql.Driver;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.docker.compose.service.connection.test.AbstractDockerComposeIntegrationTests;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link OracleJdbcDockerComposeConnectionDetailsFactory}
 *
 * @author Andy Wilkinson
 */
class OracleJdbcDockerComposeConnectionDetailsFactoryIntegrationTests extends AbstractDockerComposeIntegrationTests {

	OracleJdbcDockerComposeConnectionDetailsFactoryIntegrationTests() {
		super("oracle-compose.yaml");
	}

	@Test
	@SuppressWarnings("unchecked")
	void runCreatesConnectionDetailsThatCanBeUsedToAccessDatabase() throws Exception {
		JdbcConnectionDetails connectionDetails = run(JdbcConnectionDetails.class);
		assertThat(connectionDetails.getUsername()).isEqualTo("system");
		assertThat(connectionDetails.getPassword()).isEqualTo("secret");
		assertThat(connectionDetails.getJdbcUrl()).startsWith("jdbc:oracle:thin:@").endsWith("/xepdb1");
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setUrl(connectionDetails.getJdbcUrl());
		dataSource.setUsername(connectionDetails.getUsername());
		dataSource.setPassword(connectionDetails.getPassword());
		dataSource.setDriverClass((Class<? extends Driver>) ClassUtils.forName(connectionDetails.getDriverClassName(),
				getClass().getClassLoader()));
		JdbcTemplate template = new JdbcTemplate(dataSource);
		assertThat(template.queryForObject(DatabaseDriver.ORACLE.getValidationQuery(), String.class))
			.isEqualTo("Hello");
	}

}
