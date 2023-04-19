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

package org.springframework.boot.testcontainers.service.connection.activemq;

import java.util.Map;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

/**
 * {@link ContainerConnectionDetailsFactory} to create {@link ActiveMQConnectionDetails}
 * from a {@link ServiceConnection @ServiceConnection}-annotated {@link GenericContainer}
 * using the {@code "symptoma/activemq"} image.
 *
 * @author Eddú Meléndez
 */
class ActiveMQContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<ActiveMQConnectionDetails, Container<?>> {

	ActiveMQContainerConnectionDetailsFactory() {
		super("symptoma/activemq");
	}

	@Override
	protected ActiveMQConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
		return new ActiveMQContainerConnectionDetails(source);
	}

	private static final class ActiveMQContainerConnectionDetails extends ContainerConnectionDetails
			implements ActiveMQConnectionDetails {

		private final String brokerUrl;

		private final Map<String, String> envVars;

		private ActiveMQContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
			super(source);
			this.brokerUrl = "tcp://" + source.getContainer().getHost() + ":"
					+ source.getContainer().getFirstMappedPort();
			this.envVars = source.getContainer().getEnvMap();
		}

		@Override
		public String getBrokerUrl() {
			return this.brokerUrl;
		}

		@Override
		public String getUser() {
			return this.envVars.get("ACTIVEMQ_USERNAME");
		}

		@Override
		public String getPassword() {
			return this.envVars.get("ACTIVEMQ_PASSWORD");
		}

	}

}
