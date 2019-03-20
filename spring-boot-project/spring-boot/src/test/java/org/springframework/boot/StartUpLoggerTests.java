/*
 * Copyright 2012-2017 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link StartupInfoLogger}.
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 */
public class StartUpLoggerTests {

	private final Log log = mock(Log.class);

	@Test
	public void sourceClassIncluded() {
		given(this.log.isInfoEnabled()).willReturn(true);
		new StartupInfoLogger(getClass()).logStarting(this.log);
		verify(this.log).info(startsWith("Starting " + getClass().getSimpleName()));
	}

}
