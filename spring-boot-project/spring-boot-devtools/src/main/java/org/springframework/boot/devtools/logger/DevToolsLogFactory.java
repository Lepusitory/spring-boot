/*
 * Copyright 2018-2018 the original author or authors.
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

package org.springframework.boot.devtools.logger;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.AbstractPageRequest;

/**
 * Devtools deferred logging support.
 *
 * @author Phillip Webb
 * @since 2.1.0
 */
public final class DevToolsLogFactory {

	private static final Map<DeferredLog, Class<?>> logs = new LinkedHashMap<>();

	private DevToolsLogFactory() {
	}

	/**
	 * Get a {@link DeferredLog} instance for the specified source that will be
	 * automatically {@link DeferredLog#switchTo(Class) switched} then the
	 * {@link AbstractPageRequest context is prepared}.
	 * @param source the source for logging
	 * @return a {@link DeferredLog} instance
	 */
	public static DeferredLog getLog(Class<?> source) {
		synchronized (logs) {
			DeferredLog log = new DeferredLog();
			logs.put(log, source);
			return log;
		}
	}

	/**
	 * Listener used to log and switch when the context is ready.
	 */
	static class Listener implements ApplicationListener<ApplicationPreparedEvent> {

		@Override
		public void onApplicationEvent(ApplicationPreparedEvent event) {
			synchronized (logs) {
				logs.forEach((log, source) -> log.switchTo(source));
				logs.clear();
			}
		}

	}

}
