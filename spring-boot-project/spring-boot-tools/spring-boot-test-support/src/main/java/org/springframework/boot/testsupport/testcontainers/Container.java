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

package org.springframework.boot.testsupport.testcontainers;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assumptions;
import org.junit.rules.TestRule;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startable;

/**
 * {@link TestRule} for working with an optional Docker environment. Spins up a
 * {@link GenericContainer} if a valid docker environment is found.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
class Container implements Startable {

	private final int port;

	private final Supplier<GenericContainer<?>> containerFactory;

	private GenericContainer<?> container;

	<T extends GenericContainer<T>> Container(String dockerImageName, int port) {
		this(dockerImageName, port, null);
	}

	@SuppressWarnings({ "unchecked", "resource" })
	<T extends GenericContainer<T>> Container(String dockerImageName, int port, Consumer<T> customizer) {
		this.port = port;
		this.containerFactory = () -> {
			T container = (T) new GenericContainer<>(dockerImageName).withExposedPorts(port);
			if (customizer != null) {
				customizer.accept(container);
			}
			return container;
		};
	}

	public int getMappedPort() {
		return this.container.getMappedPort(this.port);
	}

	protected GenericContainer<?> getContainer() {
		return this.container;
	}

	@Override
	public void start() {
		Assumptions.assumeTrue(isDockerRunning(), "Could not find valid docker environment.");
		this.container = this.containerFactory.get();
		this.container.start();
	}

	private boolean isDockerRunning() {
		try {
			DockerClientFactory.instance().client();
			return true;
		}
		catch (Throwable ex) {
			return false;
		}
	}

	@Override
	public void stop() {
		if (this.container != null) {
			this.container.stop();
		}
	}

}
