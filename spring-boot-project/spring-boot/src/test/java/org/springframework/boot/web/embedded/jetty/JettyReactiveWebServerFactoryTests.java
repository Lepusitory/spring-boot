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

package org.springframework.boot.web.embedded.jetty;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.springframework.boot.web.reactive.server.AbstractReactiveWebServerFactoryTests;
import org.springframework.boot.web.server.Shutdown;
import org.springframework.http.client.reactive.JettyResourceFactory;
import org.springframework.http.server.reactive.HttpHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link JettyReactiveWebServerFactory} and {@link JettyWebServer}.
 *
 * @author Brian Clozel
 * @author Madhura Bhave
 */
class JettyReactiveWebServerFactoryTests extends AbstractReactiveWebServerFactoryTests {

	@Override
	protected JettyReactiveWebServerFactory getFactory() {
		return new JettyReactiveWebServerFactory(0);
	}

	@Test
	void setNullServerCustomizersShouldThrowException() {
		JettyReactiveWebServerFactory factory = getFactory();
		assertThatIllegalArgumentException().isThrownBy(() -> factory.setServerCustomizers(null))
				.withMessageContaining("Customizers must not be null");
	}

	@Test
	void addNullServerCustomizersShouldThrowException() {
		JettyReactiveWebServerFactory factory = getFactory();
		assertThatIllegalArgumentException()
				.isThrownBy(() -> factory.addServerCustomizers((JettyServerCustomizer[]) null))
				.withMessageContaining("Customizers must not be null");
	}

	@Test
	void jettyCustomizersShouldBeInvoked() {
		HttpHandler handler = mock(HttpHandler.class);
		JettyReactiveWebServerFactory factory = getFactory();
		JettyServerCustomizer[] configurations = new JettyServerCustomizer[4];
		Arrays.setAll(configurations, (i) -> mock(JettyServerCustomizer.class));
		factory.setServerCustomizers(Arrays.asList(configurations[0], configurations[1]));
		factory.addServerCustomizers(configurations[2], configurations[3]);
		this.webServer = factory.getWebServer(handler);
		InOrder ordered = inOrder((Object[]) configurations);
		for (JettyServerCustomizer configuration : configurations) {
			ordered.verify(configuration).customize(any(Server.class));
		}
	}

	@Test
	void specificIPAddressNotReverseResolved() throws Exception {
		JettyReactiveWebServerFactory factory = getFactory();
		InetAddress localhost = InetAddress.getLocalHost();
		factory.setAddress(InetAddress.getByAddress(localhost.getAddress()));
		this.webServer = factory.getWebServer(mock(HttpHandler.class));
		this.webServer.start();
		Connector connector = ((JettyWebServer) this.webServer).getServer().getConnectors()[0];
		assertThat(((ServerConnector) connector).getHost()).isEqualTo(localhost.getHostAddress());
	}

	@Test
	void useForwardedHeaders() {
		JettyReactiveWebServerFactory factory = getFactory();
		factory.setUseForwardHeaders(true);
		assertForwardHeaderIsUsed(factory);
	}

	@Test
	void useServerResources() throws Exception {
		JettyResourceFactory resourceFactory = new JettyResourceFactory();
		resourceFactory.afterPropertiesSet();
		JettyReactiveWebServerFactory factory = getFactory();
		factory.setResourceFactory(resourceFactory);
		JettyWebServer webServer = (JettyWebServer) factory.getWebServer(new EchoHandler());
		webServer.start();
		Connector connector = webServer.getServer().getConnectors()[0];
		assertThat(connector.getByteBufferPool()).isEqualTo(resourceFactory.getByteBufferPool());
		assertThat(connector.getExecutor()).isEqualTo(resourceFactory.getExecutor());
		assertThat(connector.getScheduler()).isEqualTo(resourceFactory.getScheduler());
	}

	@Test
	void whenServerIsShuttingDownGracefullyThenNewConnectionsCannotBeMade() throws Exception {
		JettyReactiveWebServerFactory factory = getFactory();
		Shutdown shutdown = new Shutdown();
		shutdown.setGracePeriod(Duration.ofSeconds(5));
		factory.setShutdown(shutdown);
		BlockingHandler blockingHandler = new BlockingHandler();
		this.webServer = factory.getWebServer(blockingHandler);
		this.webServer.start();
		getWebClient().build().get().retrieve().toBodilessEntity().subscribe();
		blockingHandler.awaitQueue();
		Future<Boolean> shutdownResult = initiateGracefulShutdown();
		// We need to make two requests as Jetty accepts one additional request after a
		// connector has been told to stop accepting requests
		CountDownLatch responseLatch = new CountDownLatch(1);
		AtomicReference<Throwable> errorReference = new AtomicReference<>();
		getWebClient().build().get().retrieve().toBodilessEntity().doOnSuccess((response) -> responseLatch.countDown())
				.doOnError(errorReference::set).subscribe();
		getWebClient().build().get().retrieve().toBodilessEntity().doOnSuccess((response) -> responseLatch.countDown())
				.doOnError(errorReference::set).subscribe();
		assertThat(shutdownResult.get()).isEqualTo(false);
		blockingHandler.completeOne();
		blockingHandler.completeOne();
		responseLatch.await(5, TimeUnit.SECONDS);
		this.webServer.stop();
		Throwable error = await().atMost(Duration.ofSeconds(30)).until(errorReference::get, (ex) -> ex != null);
		assertThat(error).isInstanceOf(IOException.class);
	}

	@Override
	protected boolean inGracefulShutdown() {
		return ((JettyWebServer) this.webServer).inGracefulShutdown();
	}

}
