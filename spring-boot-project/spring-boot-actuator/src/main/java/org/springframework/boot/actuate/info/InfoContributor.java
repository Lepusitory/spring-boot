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

package org.springframework.boot.actuate.info;

/**
 * Contributes additional info details.
 *
 * @author Stephane Nicoll
 * @since 1.4.0
 */
@FunctionalInterface
public interface InfoContributor {

	/**
	 * Contributes additional details using the specified {@link Info.Builder Builder}.
	 * @param builder the builder to use
	 */
	void contribute(Info.Builder builder);

}
