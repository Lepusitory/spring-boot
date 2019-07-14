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

package smoketest.jooq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SampleJooqApplication}.
 */
@ExtendWith(OutputCaptureExtension.class)
class SampleJooqApplicationTests {

	private static final String[] NO_ARGS = {};

	@Test
	void outputResults(CapturedOutput output) {
		SampleJooqApplication.main(NO_ARGS);
		assertThat(output).contains("jOOQ Fetch 1 Greg Turnquest").contains("jOOQ Fetch 2 Craig Walls")
				.contains("jOOQ SQL " + "[Learning Spring Boot : Greg Turnquest, "
						+ "Spring Boot in Action : Craig Walls]");
	}

}
