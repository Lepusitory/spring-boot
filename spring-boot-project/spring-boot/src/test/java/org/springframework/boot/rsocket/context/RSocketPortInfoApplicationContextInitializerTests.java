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
package org.springframework.boot.rsocket.context;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.boot.rsocket.server.RSocketServerException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class RSocketPortInfoApplicationContextInitializerTests {

	@Autowired
	private RSocketPortInfoApplicationContextInitializer initializer;

	@Autowired
	private ConfigurableApplicationContext context;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Test
	void nullAddressDoesNotThrow() {
		initializer.initialize(context);
		publisher.publishEvent(new RSocketServerInitializedEvent(new RSocketServer() {
			@Override
			public void start() throws RSocketServerException {

			}

			@Override
			public void stop() throws RSocketServerException {

			}

			@Override
			public InetSocketAddress address() {
				return null;
			}
		}));
	}

	@Configuration(proxyBeanMethods = false)
	static class Config {

		@Bean
		public RSocketPortInfoApplicationContextInitializer rSocketPortInfoApplicationContextInitializer() {
			return new RSocketPortInfoApplicationContextInitializer();
		}
	}
}
