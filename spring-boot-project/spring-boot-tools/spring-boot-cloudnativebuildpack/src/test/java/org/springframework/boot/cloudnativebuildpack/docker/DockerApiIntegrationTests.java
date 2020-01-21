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

package org.springframework.boot.cloudnativebuildpack.docker;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.springframework.boot.cloudnativebuildpack.docker.type.ImageReference;
import org.springframework.boot.testsupport.testcontainers.DisabledIfDockerUnavailable;

/**
 * Integration tests for {@link DockerApi}.
 *
 * @author Phillip Webb
 */
@DisabledIfDockerUnavailable
class DockerApiIntegrationTests {

	private final DockerApi docker = new DockerApi();

	@Test
	void pullImage() throws IOException {
		this.docker.image().pull(ImageReference.of("cloudfoundry/cnb:bionic"),
				new TotalProgressPullListener(new TotalProgressBar("Pulling: ")));
	}

}
