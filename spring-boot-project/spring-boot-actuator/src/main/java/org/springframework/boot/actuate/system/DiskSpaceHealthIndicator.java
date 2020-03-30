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

package org.springframework.boot.actuate.system;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.log.LogMessage;
import org.springframework.util.unit.DataSize;

/**
 * A {@link HealthIndicator} that checks available disk space and reports a status of
 * {@link Status#DOWN} when it drops below a configurable threshold.
 *
 * @author Mattias Severson
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public class DiskSpaceHealthIndicator extends AbstractHealthIndicator {

	private static final Log logger = LogFactory.getLog(DiskSpaceHealthIndicator.class);

	private final File path;

	private final DataSize threshold;

	/**
	 * Create a new {@code DiskSpaceHealthIndicator} instance.
	 * @param path the Path used to compute the available disk space
	 * @param threshold the minimum disk space that should be available
	 */
	public DiskSpaceHealthIndicator(File path, DataSize threshold) {
		super("DiskSpace health check failed");
		this.path = path;
		this.threshold = threshold;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		long diskFreeInBytes = this.path.getUsableSpace();
		// return value of 0L means "the abstract pathname does not name a
		// partition" which for our purposes means it's not usable i.e DOWN
		if (diskFreeInBytes >= this.threshold.toBytes() && diskFreeInBytes != 0L) {
			builder.up();
		}
		else {
			logger.warn(LogMessage.format("Free disk space below threshold. Available: %d bytes (threshold: %s)",
					diskFreeInBytes, this.threshold));
			builder.down();
		}
		builder.withDetail("total", this.path.getTotalSpace()).withDetail("free", diskFreeInBytes)
				.withDetail("threshold", this.threshold.toBytes()).withDetail("exists", this.path.exists())
				.withDetail("canRead", this.path.canRead()).withDetail("canWrite", this.path.canWrite())
				.withDetail("canExecute", this.path.canExecute());
	}

}
