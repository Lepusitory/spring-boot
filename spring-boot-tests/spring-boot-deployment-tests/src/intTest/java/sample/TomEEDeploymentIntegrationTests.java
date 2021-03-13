/*
 * Copyright 2012-2021 the original author or authors.
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

package sample;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Deployment integration tests for TomEE.
 *
 * @author Christoph Dreis
 */
@Testcontainers(disabledWithoutDocker = true)
class TomEEDeploymentIntegrationTests extends AbstractDeploymentIntegrationTests {

	@Container
	static WarDeploymentContainer container = new WarDeploymentContainer("tomee:8-jre-8.0.2-webprofile",
			"/usr/local/tomee/webapps", DEFAULT_PORT);

	@Override
	WarDeploymentContainer getContainer() {
		return container;
	}

}
