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

package org.springframework.boot.logging;

import org.apache.commons.logging.Log;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link DeferredLogFactory}.
 *
 * @author Phillip Webb
 */
class DeferredLogFactoryTests {

	private final DeferredLogFactory factory = (supplier) -> this.log = supplier.get();

	private Log log;

	@Test
	void getLogFromClassCreatesLogSupplier() {
		this.factory.getLog(DeferredLogFactoryTests.class);
		assertThat(this.log).isNotNull();
	}

	@Test
	void getLogFromDestinationCreatesLogSupplier() {
		Log log = mock(Log.class);
		this.factory.getLog(log);
		assertThat(this.log).isSameAs(log);
	}

}
