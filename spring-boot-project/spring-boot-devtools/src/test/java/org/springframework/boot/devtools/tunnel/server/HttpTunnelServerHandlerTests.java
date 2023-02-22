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

package org.springframework.boot.devtools.tunnel.server;

import org.junit.jupiter.api.Test;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link HttpTunnelServerHandler}.
 *
 * @author Phillip Webb
 */
class HttpTunnelServerHandlerTests {

	@Test
	void serverMustNotBeNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> new HttpTunnelServerHandler(null))
			.withMessageContaining("Server must not be null");
	}

	@Test
	void handleDelegatesToServer() throws Exception {
		HttpTunnelServer server = mock(HttpTunnelServer.class);
		HttpTunnelServerHandler handler = new HttpTunnelServerHandler(server);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		ServerHttpResponse response = mock(ServerHttpResponse.class);
		handler.handle(request, response);
		then(server).should().handle(request, response);
	}

}
