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

package org.springframework.boot.rsocket.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.rsocket.SocketAcceptor;
import io.rsocket.transport.ServerTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.TcpServer;

import org.springframework.boot.rsocket.server.ConfigurableRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.boot.rsocket.server.RSocketServerFactory;
import org.springframework.boot.web.embedded.netty.SslServerCustomizer;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.util.Assert;

/**
 * {@link RSocketServerFactory} that can be used to create {@link RSocketServer}s backed
 * by Netty.
 *
 * @author Brian Clozel
 * @author Chris Bono
 * @since 2.2.0
 */
public class NettyRSocketServerFactory implements RSocketServerFactory, ConfigurableRSocketServerFactory {

	private int port = 9898;

	private InetAddress address;

	private RSocketServer.Transport transport = RSocketServer.Transport.TCP;

	private ReactorResourceFactory resourceFactory;

	private Duration lifecycleTimeout;

	private List<RSocketServerCustomizer> rSocketServerCustomizers = new ArrayList<>();

	private Ssl ssl;

	private SslStoreProvider sslStoreProvider;

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	@Override
	public void setTransport(RSocketServer.Transport transport) {
		this.transport = transport;
	}

	@Override
	public void setSsl(Ssl ssl) {
		this.ssl = ssl;
	}

	@Override
	public void setSslStoreProvider(SslStoreProvider sslStoreProvider) {
		this.sslStoreProvider = sslStoreProvider;
	}

	/**
	 * Set the {@link ReactorResourceFactory} to get the shared resources from.
	 * @param resourceFactory the server resources
	 */
	public void setResourceFactory(ReactorResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

	/**
	 * Set {@link RSocketServerCustomizer}s that should be called to configure the
	 * {@link io.rsocket.core.RSocketServer} while building the server. Calling this
	 * method will replace any existing customizers.
	 * @param rSocketServerCustomizers customizers to apply before the server starts
	 * @since 2.2.7
	 */
	public void setRSocketServerCustomizers(Collection<? extends RSocketServerCustomizer> rSocketServerCustomizers) {
		Assert.notNull(rSocketServerCustomizers, "RSocketServerCustomizers must not be null");
		this.rSocketServerCustomizers = new ArrayList<>(rSocketServerCustomizers);
	}

	/**
	 * Add {@link RSocketServerCustomizer}s that should be called to configure the
	 * {@link io.rsocket.core.RSocketServer}.
	 * @param rSocketServerCustomizers customizers to apply before the server starts
	 * @since 2.2.7
	 */
	public void addRSocketServerCustomizers(RSocketServerCustomizer... rSocketServerCustomizers) {
		Assert.notNull(rSocketServerCustomizers, "RSocketServerCustomizers must not be null");
		this.rSocketServerCustomizers.addAll(Arrays.asList(rSocketServerCustomizers));
	}

	/**
	 * Set the maximum amount of time that should be waited when starting or stopping the
	 * server.
	 * @param lifecycleTimeout the lifecycle timeout
	 */
	public void setLifecycleTimeout(Duration lifecycleTimeout) {
		this.lifecycleTimeout = lifecycleTimeout;
	}

	@Override
	public NettyRSocketServer create(SocketAcceptor socketAcceptor) {
		ServerTransport<CloseableChannel> transport = createTransport();
		io.rsocket.core.RSocketServer server = io.rsocket.core.RSocketServer.create(socketAcceptor);
		this.rSocketServerCustomizers.forEach((customizer) -> customizer.customize(server));
		Mono<CloseableChannel> starter = server.bind(transport);
		return new NettyRSocketServer(starter, this.lifecycleTimeout);
	}

	private ServerTransport<CloseableChannel> createTransport() {
		if (this.transport == RSocketServer.Transport.WEBSOCKET) {
			return createWebSocketTransport();
		}
		return createTcpTransport();
	}

	private ServerTransport<CloseableChannel> createWebSocketTransport() {
		HttpServer httpServer;
		if (this.resourceFactory != null) {
			httpServer = HttpServer.create().runOn(this.resourceFactory.getLoopResources())
					.bindAddress(this::getListenAddress);
		}
		else {
			InetSocketAddress listenAddress = this.getListenAddress();
			httpServer = HttpServer.create().host(listenAddress.getHostName()).port(listenAddress.getPort());
		}

		if (this.ssl != null && this.ssl.isEnabled()) {
			SslServerCustomizer sslServerCustomizer = new SslServerCustomizer(this.ssl, null, this.sslStoreProvider);
			httpServer = sslServerCustomizer.apply(httpServer);
		}

		return WebsocketServerTransport.create(httpServer);
	}

	private ServerTransport<CloseableChannel> createTcpTransport() {
		TcpServer tcpServer;
		if (this.resourceFactory != null) {
			tcpServer = TcpServer.create().runOn(this.resourceFactory.getLoopResources())
					.bindAddress(this::getListenAddress);
		}
		else {
			InetSocketAddress listenAddress = this.getListenAddress();
			tcpServer = TcpServer.create().host(listenAddress.getHostName()).port(listenAddress.getPort());
		}

		if (this.ssl != null && this.ssl.isEnabled()) {
			TcpSslServerCustomizer sslServerCustomizer = new TcpSslServerCustomizer(this.ssl, this.sslStoreProvider);
			tcpServer = sslServerCustomizer.apply(tcpServer);
		}

		return TcpServerTransport.create(tcpServer);
	}

	private InetSocketAddress getListenAddress() {
		if (this.address != null) {
			return new InetSocketAddress(this.address.getHostAddress(), this.port);
		}
		return new InetSocketAddress(this.port);
	}

	private static final class TcpSslServerCustomizer extends SslServerCustomizer {

		private TcpSslServerCustomizer(Ssl ssl, SslStoreProvider sslStoreProvider) {
			super(ssl, null, sslStoreProvider);
		}

		// This does not override the apply in parent - currently just leveraging the
		// parent for its "getContextBuilder()" method. This should be refactored when
		// we add the concept of http/tcp customizers for RSocket.
		private TcpServer apply(TcpServer server) {
			try {
				return server.secure((contextSpec) -> contextSpec.sslContext(getContextBuilder()));
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}

	}

}
