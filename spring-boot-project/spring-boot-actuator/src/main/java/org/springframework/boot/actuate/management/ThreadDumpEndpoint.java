/*
 * Copyright 2012-2022 the original author or authors.
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

package org.springframework.boot.actuate.management;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.NativeDetector;

/**
 * {@link Endpoint @Endpoint} to expose thread info.
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 * @since 2.0.0
 */
@Endpoint(id = "threaddump")
public class ThreadDumpEndpoint {

	private final PlainTextThreadDumpFormatter plainTextFormatter = new PlainTextThreadDumpFormatter();

	@ReadOperation
	public ThreadDumpDescriptor threadDump() {
		return getFormattedThreadDump(ThreadDumpDescriptor::new);
	}

	@ReadOperation(produces = "text/plain;charset=UTF-8")
	public String textThreadDump() {
		return getFormattedThreadDump(this.plainTextFormatter::format);
	}

	private <T> T getFormattedThreadDump(Function<ThreadInfo[], T> formatter) {
		ThreadDumper threadDumper = createThreadDumper();
		return formatter.apply(threadDumper.dumpAllThreads());
	}

	private ThreadDumper createThreadDumper() {
		if (NativeDetector.inNativeImage()) {
			throw new ThreadDumperUnavailableException("Running in native image");
		}
		return new ThreadMXBeanThreadDumper();
	}

	private interface ThreadDumper {

		ThreadInfo[] dumpAllThreads();

	}

	private static class ThreadMXBeanThreadDumper implements ThreadDumper {

		@Override
		public ThreadInfo[] dumpAllThreads() {
			return ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
		}

	}

	/**
	 * Exception to be thrown if the {@link ThreadDumper} cannot be created.
	 */
	static class ThreadDumperUnavailableException extends RuntimeException {

		ThreadDumperUnavailableException(String message) {
			super(message);
		}

	}

	/**
	 * A description of a thread dump. Primarily intended for serialization to JSON.
	 */
	public static final class ThreadDumpDescriptor {

		private final List<ThreadInfo> threads;

		private ThreadDumpDescriptor(ThreadInfo[] threads) {
			this.threads = Arrays.asList(threads);
		}

		public List<ThreadInfo> getThreads() {
			return this.threads;
		}

	}

}
