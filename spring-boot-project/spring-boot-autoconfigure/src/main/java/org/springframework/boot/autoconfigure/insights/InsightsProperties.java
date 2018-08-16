/*
 * Copyright 2012-2018 the original author or authors.
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

package org.springframework.boot.autoconfigure.insights;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link ConfigurationProperties properties} for .
 *
 * @author Brian Clozel
 * @since 2.1.0
 */
@ConfigurationProperties(prefix = "spring.insights")
public class InsightsProperties {

	private final Web web = new Web();

	public Web getWeb() {
		return this.web;
	}

	public static class Web {

		/**
		 * Whether logging of (potentially sensitive) request details at DEBUG and TRACE
		 * level is allowed.
		 */
		private boolean logRequestDetails = false;

		public boolean isLogRequestDetails() {
			return this.logRequestDetails;
		}

		public void setLogRequestDetails(boolean logRequestDetails) {
			this.logRequestDetails = logRequestDetails;
		}

	}

}
