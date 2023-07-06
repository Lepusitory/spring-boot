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

package org.springframework.boot.web.client;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.boot.testsupport.classpath.ClassPathExclusions;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests for {@link ClientHttpRequestFactories} when JDK HttpClient is the
 * predominant HTTP client.
 *
 * @author Andy Wilkinson
 */
@ClassPathExclusions({ "httpclient5-*.jar", "okhttp-*.jar" })
class ClientHttpRequestFactoriesJdkClientTests
		extends AbstractClientHttpRequestFactoriesTests<JdkClientHttpRequestFactory> {

	ClientHttpRequestFactoriesJdkClientTests() {
		super(JdkClientHttpRequestFactory.class);
	}

	@Override
	protected long connectTimeout(JdkClientHttpRequestFactory requestFactory) {
		HttpClient httpClient = (HttpClient) ReflectionTestUtils.getField(requestFactory, "httpClient");
		return httpClient.connectTimeout().map(Duration::toMillis).orElse(-1L);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected long readTimeout(JdkClientHttpRequestFactory requestFactory) {
		return ((Duration) ReflectionTestUtils.getField(requestFactory, "readTimeout")).toMillis();
	}

}
