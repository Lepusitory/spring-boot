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

package org.springframework.boot.web.embedded.tomcat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.catalina.Container;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.web.server.GracefulShutdown;

/**
 * {@link GracefulShutdown} for {@link Tomcat}.
 *
 * @author Andy Wilkinson
 */
class TomcatGracefulShutdown implements GracefulShutdown {

	private static final Log logger = LogFactory.getLog(TomcatGracefulShutdown.class);

	private final Tomcat tomcat;

	private final Duration period;

	private volatile boolean shuttingDown = false;

	TomcatGracefulShutdown(Tomcat tomcat, Duration period) {
		this.tomcat = tomcat;
		this.period = period;
	}

	@Override
	public boolean shutDownGracefully() {
		logger.info("Commencing graceful shutdown, allowing up to " + this.period.getSeconds()
				+ "s for active requests to complete");
		List<Connector> connectors = getConnectors();
		for (Connector connector : connectors) {
			connector.pause();
			connector.getProtocolHandler().closeServerSocketGraceful();
		}
		this.shuttingDown = true;
		try {
			long end = System.currentTimeMillis() + this.period.toMillis();
			for (Container host : this.tomcat.getEngine().findChildren()) {
				for (Container context : host.findChildren()) {
					while (active(context)) {
						if (System.currentTimeMillis() > end) {
							logger.info("Grace period elapsed with one or more requests still active");
							return false;
						}
						Thread.sleep(50);
					}
				}
			}

		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		finally {
			this.shuttingDown = false;
		}
		logger.info("Graceful shutdown complete");
		return true;
	}

	private boolean active(Container context) {
		try {
			if (((StandardContext) context).getInProgressAsyncCount() > 0) {
				return true;
			}
			for (Container wrapper : context.findChildren()) {
				if (((StandardWrapper) wrapper).getCountAllocated() > 0) {
					return true;
				}
			}
			return false;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private List<Connector> getConnectors() {
		List<Connector> connectors = new ArrayList<>();
		for (Service service : this.tomcat.getServer().findServices()) {
			Collections.addAll(connectors, service.findConnectors());
		}
		return connectors;
	}

	@Override
	public boolean isShuttingDown() {
		return this.shuttingDown;
	}

}
