/*
 * Copyright 2012-2022 the original author or authors.
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

package org.springframework.boot.actuate.integration;

import org.junit.jupiter.api.Test;

import org.springframework.integration.graph.Graph;
import org.springframework.integration.graph.IntegrationGraphServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link IntegrationGraphEndpoint}.
 *
 * @author Tim Ysewyn
 */
class IntegrationGraphEndpointTests {

	private final IntegrationGraphServer server = mock(IntegrationGraphServer.class);

	private final IntegrationGraphEndpoint endpoint = new IntegrationGraphEndpoint(this.server);

	@Test
	void readOperationShouldReturnGraph() {
		Graph mockedGraph = mock(Graph.class);
		given(this.server.getGraph()).willReturn(mockedGraph);
		Graph graph = this.endpoint.graph();
		then(this.server).should().getGraph();
		assertThat(graph).isEqualTo(mockedGraph);
	}

	@Test
	void writeOperationShouldRebuildGraph() {
		this.endpoint.rebuild();
		then(this.server).should().rebuild();
	}

}
