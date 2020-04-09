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

package org.springframework.boot.actuate.availability;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;

/**
 * A {@link HealthIndicator} that checks the {@link LivenessState} of the application.
 *
 * @author Brian Clozel
 * @since 2.3.0
 */
public class LivenessProbeHealthIndicator extends AbstractHealthIndicator {

	private final ApplicationAvailability applicationAvailability;

	public LivenessProbeHealthIndicator(ApplicationAvailability applicationAvailability) {
		this.applicationAvailability = applicationAvailability;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		LivenessState state = this.applicationAvailability.getLivenessState();
		builder.status(LivenessState.CORRECT == state ? Status.UP : Status.DOWN);
	}

}
