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

package org.springframework.boot.context.config;

import org.apache.commons.logging.Log;

import org.springframework.core.log.LogMessage;

/**
 * Action to take when an uncaught {@link ConfigDataLocationNotFoundException} is thrown.
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public enum ConfigDataLocationNotFoundAction {

	/**
	 * Throw the exception to fail startup.
	 */
	FAIL {

		@Override
		void handle(Log logger, Object location, ConfigDataLocationNotFoundException ex) {
			throw ex;
		}

	},

	/**
	 * Ignore the exception and continue processing the remaining locations.
	 */
	IGNORE {

		@Override
		void handle(Log logger, Object location, ConfigDataLocationNotFoundException ex) {
			logger.trace(LogMessage.format("Ignoring missing resource from location %s", location));
		}

	};

	/**
	 * Handle the given exception.
	 * @param logger the logger used for output
	 * @param location the location being checked (a {@link ConfigDataLocation} or
	 * {@code String})
	 * @param ex the exception to handle
	 */
	abstract void handle(Log logger, Object location, ConfigDataLocationNotFoundException ex);

}
