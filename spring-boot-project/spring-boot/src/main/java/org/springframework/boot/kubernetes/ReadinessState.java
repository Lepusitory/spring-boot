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

package org.springframework.boot.kubernetes;

import java.util.Objects;

/**
 * "Readiness" state of the application, when deployed on Kubernetes.
 * <p>
 * An application is considered ready when it's {@link LivenessState live} and willing to
 * accept traffic. "Readiness" failure means that the application is not able to accept
 * traffic and that the infrastructure should stop routing requests to it.
 *
 * @author Brian Clozel
 * @since 2.3.0
 */
public final class ReadinessState {

	private final Availability availability;

	private ReadinessState(Availability availability) {
		this.availability = availability;
	}

	public Availability getAvailability() {
		return this.availability;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ReadinessState that = (ReadinessState) o;
		return this.availability == that.availability;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.availability);
	}

	@Override
	public String toString() {
		return "ReadinessState{" + "availability=" + this.availability + '}';
	}

	public static ReadinessState ready() {
		return new ReadinessState(Availability.READY);
	}

	public static ReadinessState busy() {
		return new ReadinessState(Availability.BUSY);
	}

	public enum Availability {

		/**
		 * The application is not willing to receive traffic.
		 */
		BUSY,
		/**
		 * The application is ready to receive traffic.
		 */
		READY

	}

}
