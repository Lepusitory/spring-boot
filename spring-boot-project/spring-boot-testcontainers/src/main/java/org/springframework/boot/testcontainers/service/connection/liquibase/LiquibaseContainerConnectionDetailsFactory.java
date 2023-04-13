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

package org.springframework.boot.testcontainers.service.connection.liquibase;

import org.testcontainers.containers.JdbcDatabaseContainer;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

/**
 * {@link ContainerConnectionDetailsFactory} to create {@link LiquibaseConnectionDetails}
 * from a {@link ServiceConnection @ServiceConnection}-annotated
 * {@link JdbcDatabaseContainer}.
 *
 * @author Andy Wilkinson
 */
class LiquibaseContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<LiquibaseConnectionDetails, JdbcDatabaseContainer<?>> {

	@Override
	protected LiquibaseConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<LiquibaseConnectionDetails, JdbcDatabaseContainer<?>> source) {
		return new LiquibaseContainerConnectionDetails(source);
	}

	/**
	 * {@link LiquibaseConnectionDetails} backed by a {@link JdbcDatabaseContainer}.
	 */
	private static final class LiquibaseContainerConnectionDetails extends ContainerConnectionDetails
			implements LiquibaseConnectionDetails {

		private final JdbcDatabaseContainer<?> container;

		private LiquibaseContainerConnectionDetails(
				ContainerConnectionSource<LiquibaseConnectionDetails, JdbcDatabaseContainer<?>> source) {
			super(source);
			this.container = source.getContainer();
		}

		@Override
		public String getUsername() {
			return this.container.getUsername();
		}

		@Override
		public String getPassword() {
			return this.container.getPassword();
		}

		@Override
		public String getJdbcUrl() {
			return this.container.getJdbcUrl();
		}

		@Override
		public String getDriverClassName() {
			return this.container.getDriverClassName();
		}

	}

}
