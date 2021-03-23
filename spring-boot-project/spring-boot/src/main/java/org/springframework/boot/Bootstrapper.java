/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.boot;

/**
 * Callback interface that can be used to initialize a {@link BootstrapRegistry} before it
 * is used.
 *
 * @author Phillip Webb
 * @since 2.4.0
 * @see SpringApplication#addBootstrapper(Bootstrapper)
 * @see BootstrapRegistry
 */
public interface Bootstrapper {

	/**
	 * Initialize the given {@link BootstrapRegistry} with any required registrations.
	 * @param registry the registry to initialize
	 * @since 2.4.4
	 */
	default void initialize(BootstrapRegistry registry) {
		intitialize(registry);
	}

	/**
	 * Initialize the given {@link BootstrapRegistry} with any required registrations.
	 * @param registry the registry to initialize
	 * @deprecated since 2.4.4 in favor of
	 * {@link Bootstrapper#initialize(BootstrapRegistry)}
	 */
	@Deprecated
	void intitialize(BootstrapRegistry registry);

}
