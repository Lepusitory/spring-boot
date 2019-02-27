/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.actuate.web.trace.reactive;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ServerWebExchangeTraceableRequest}.
 *
 * @author Dmytro Nosan
 */
public class ServerWebExchangeTraceableRequestTests {

	private ServerWebExchange exchange;

	private ServerHttpRequest request;

	@Before
	public void setUp() {
		this.exchange = mock(ServerWebExchange.class);
		this.request = mock(ServerHttpRequest.class);
		doReturn(this.request).when(this.exchange).getRequest();
	}

	@Test
	public void getMethod() {
		String method = "POST";
		doReturn(method).when(this.request).getMethodValue();
		ServerWebExchangeTraceableRequest traceableRequest = new ServerWebExchangeTraceableRequest(
				this.exchange);
		assertThat(traceableRequest.getMethod()).isSameAs(method);
	}

	@Test
	public void getUri() {
		URI uri = URI.create("http://localhost:8080/");
		doReturn(uri).when(this.request).getURI();
		ServerWebExchangeTraceableRequest traceableRequest = new ServerWebExchangeTraceableRequest(
				this.exchange);
		assertThat(traceableRequest.getUri()).isSameAs(uri);
	}

	@Test
	public void getHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("name", "value");
		doReturn(httpHeaders).when(this.request).getHeaders();
		ServerWebExchangeTraceableRequest traceableRequest = new ServerWebExchangeTraceableRequest(
				this.exchange);
		assertThat(traceableRequest.getHeaders())
				.containsOnly(entry("name", Collections.singletonList("value")));
	}

	@Test
	public void getUnresolvedRemoteAddress() {
		InetSocketAddress socketAddress = InetSocketAddress.createUnresolved("", 0);
		doReturn(socketAddress).when(this.request).getRemoteAddress();
		ServerWebExchangeTraceableRequest traceableRequest = new ServerWebExchangeTraceableRequest(
				this.exchange);
		assertThat(traceableRequest.getRemoteAddress()).isNull();

	}

	@Test
	public void getRemoteAddress() {
		InetSocketAddress socketAddress = new InetSocketAddress(0);
		doReturn(socketAddress).when(this.request).getRemoteAddress();
		ServerWebExchangeTraceableRequest traceableRequest = new ServerWebExchangeTraceableRequest(
				this.exchange);
		assertThat(traceableRequest.getRemoteAddress())
				.isEqualTo(socketAddress.getAddress().toString());
	}

}
