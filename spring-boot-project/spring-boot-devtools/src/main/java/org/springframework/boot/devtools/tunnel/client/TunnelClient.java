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

package org.springframework.boot.devtools.tunnel.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.log.LogMessage;
import org.springframework.util.Assert;

/**
 * The client side component of a socket tunnel. Starts a {@link ServerSocket} of the
 * specified port for local clients to connect to.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.3.0
 */
public class TunnelClient implements SmartInitializingSingleton {

	private static final int BUFFER_SIZE = 1024 * 100;

	private static final Log logger = LogFactory.getLog(TunnelClient.class);

	private final TunnelClientListeners listeners = new TunnelClientListeners();

	private final Lock lock = new ReentrantLock();

	private final int listenPort;

	private final TunnelConnection tunnelConnection;

	private ServerThread serverThread;

	public TunnelClient(int listenPort, TunnelConnection tunnelConnection) {
		Assert.isTrue(listenPort >= 0, "ListenPort must be greater than or equal to 0");
		Assert.notNull(tunnelConnection, "TunnelConnection must not be null");
		this.listenPort = listenPort;
		this.tunnelConnection = tunnelConnection;
	}

	@Override
	public void afterSingletonsInstantiated() {
		this.lock.lock();
		try {
			if (this.serverThread == null) {
				try {
					start();
				}
				catch (IOException ex) {
					throw new IllegalStateException(ex);
				}
			}
		}
		finally {
			this.lock.unlock();
		}
	}

	/**
	 * Start the client and accept incoming connections.
	 * @return the port on which the client is listening
	 * @throws IOException in case of I/O errors
	 */
	public int start() throws IOException {
		this.lock.lock();
		try {
			Assert.state(this.serverThread == null, "Server already started");
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(this.listenPort));
			int port = serverSocketChannel.socket().getLocalPort();
			logger.trace(LogMessage.format("Listening for TCP traffic to tunnel on port %s", port));
			this.serverThread = new ServerThread(serverSocketChannel);
			this.serverThread.start();
			return port;
		}
		finally {
			this.lock.unlock();
		}
	}

	/**
	 * Stop the client, disconnecting any servers.
	 * @throws IOException in case of I/O errors
	 */
	public void stop() throws IOException {
		this.lock.lock();
		try {
			if (this.serverThread != null) {
				this.serverThread.close();
				try {
					this.serverThread.join(2000);
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				this.serverThread = null;
			}
		}
		finally {
			this.lock.unlock();
		}
	}

	protected final ServerThread getServerThread() {
		this.lock.lock();
		try {
			return this.serverThread;
		}
		finally {
			this.lock.unlock();
		}
	}

	public void addListener(TunnelClientListener listener) {
		this.listeners.addListener(listener);
	}

	public void removeListener(TunnelClientListener listener) {
		this.listeners.removeListener(listener);
	}

	/**
	 * The main server thread.
	 */
	protected class ServerThread extends Thread {

		private final ServerSocketChannel serverSocketChannel;

		private boolean acceptConnections = true;

		public ServerThread(ServerSocketChannel serverSocketChannel) {
			this.serverSocketChannel = serverSocketChannel;
			setName("Tunnel Server");
			setDaemon(true);
		}

		public void close() throws IOException {
			logger.trace(LogMessage.format("Closing tunnel client on port %s",
					this.serverSocketChannel.socket().getLocalPort()));
			this.serverSocketChannel.close();
			this.acceptConnections = false;
			interrupt();
		}

		@Override
		public void run() {
			try {
				while (this.acceptConnections) {
					try (SocketChannel socket = this.serverSocketChannel.accept()) {
						handleConnection(socket);
					}
					catch (AsynchronousCloseException ex) {
						// Connection has been closed. Keep the server running
					}
				}
			}
			catch (Exception ex) {
				logger.trace("Unexpected exception from tunnel client", ex);
			}
		}

		private void handleConnection(SocketChannel socketChannel) throws Exception {
			Closeable closeable = new SocketCloseable(socketChannel);
			TunnelClient.this.listeners.fireOpenEvent(socketChannel);
			try (WritableByteChannel outputChannel = TunnelClient.this.tunnelConnection.open(socketChannel,
					closeable)) {
				logger.trace(
						"Accepted connection to tunnel client from " + socketChannel.socket().getRemoteSocketAddress());
				while (true) {
					ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
					int amountRead = socketChannel.read(buffer);
					if (amountRead == -1) {
						return;
					}
					if (amountRead > 0) {
						buffer.flip();
						outputChannel.write(buffer);
					}
				}
			}
		}

		protected void stopAcceptingConnections() {
			this.acceptConnections = false;
		}

	}

	/**
	 * {@link Closeable} used to close a {@link SocketChannel} and fire an event.
	 */
	private class SocketCloseable implements Closeable {

		private final SocketChannel socketChannel;

		private boolean closed = false;

		SocketCloseable(SocketChannel socketChannel) {
			this.socketChannel = socketChannel;
		}

		@Override
		public void close() throws IOException {
			if (!this.closed) {
				this.socketChannel.close();
				TunnelClient.this.listeners.fireCloseEvent(this.socketChannel);
				this.closed = true;
			}
		}

	}

}
