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

package org.springframework.boot;

import org.springframework.boot.SpringApplicationHooks.Hook;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.Assert;

/**
 * A {@link Hook} used to prevent standard refresh of the application's context, ready for
 * subsequent {@link GenericApplicationContext#refreshForAotProcessing() AOT processing}.
 *
 * @author Andy Wilkinson
 */
class AotProcessingHook implements Hook {

	private GenericApplicationContext context;

	@Override
	public boolean preRefresh(SpringApplication application, ConfigurableApplicationContext context) {
		Assert.isInstanceOf(GenericApplicationContext.class, context,
				() -> "AOT processing requires a GenericApplicationContext but got a " + context.getClass().getName());
		this.context = (GenericApplicationContext) context;
		return false;
	}

	@Override
	public void postRun(SpringApplication application, ConfigurableApplicationContext context) {
		throw new MainMethodSilentExitException();
	}

	GenericApplicationContext getApplicationContext() {
		return this.context;
	}

	/**
	 * Internal exception used to prevent main method to continue once
	 * {@code SpringApplication#run} completes.
	 */
	static class MainMethodSilentExitException extends RuntimeException {

	}

}
