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

package org.springframework.boot.docs.howto.httpclients.webclientreactornettycustomization

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.client.reactive.ReactorResourceFactory
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient

@Configuration(proxyBeanMethods = false)
class MyReactorNettyClientConfiguration {
	@Bean
	fun clientHttpConnector(resourceFactory: ReactorResourceFactory): ClientHttpConnector {
		// @formatter:off
		val httpClient = HttpClient.create(resourceFactory.connectionProvider)
			.runOn(resourceFactory.loopResources)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
			.doOnConnected { connection: Connection ->
				connection.addHandlerLast(
					ReadTimeoutHandler(60)
				)
			}
		return ReactorClientHttpConnector(httpClient)
		// @formatter:on
	}
}