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

package org.springframework.boot.docs.jdbc;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Example configuration for configuring a simple {@link DataSource}.
 *
 * @author Stephane Nicoll
 */
public class SimpleDataSourceExample {

	/**
	 * A simple configuration that exposes dedicated settings.
	 */
	@Configuration
	static class SimpleDataSourceConfiguration {

		// tag::configuration[]
		@Bean
		@ConfigurationProperties("app.datasource")
		public HikariDataSource dataSource() {
			return DataSourceBuilder.create().type(HikariDataSource.class).build();
		}
		// end::configuration[]

	}

}
