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

package org.springframework.boot.docker.compose.lifecycle;

import org.junit.jupiter.api.Test;

import org.springframework.boot.docker.compose.core.DockerCompose;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link StartupCommand}.
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
class StartupCommandTests {

	private DockerCompose dockerCompose = mock(DockerCompose.class);

	@Test
	void applyToWhenUp() {
		StartupCommand.UP.applyTo(this.dockerCompose);
		then(this.dockerCompose).should().up();
	}

	@Test
	void applyToWhenStart() {
		StartupCommand.START.applyTo(this.dockerCompose);
		then(this.dockerCompose).should().start();
	}

}
