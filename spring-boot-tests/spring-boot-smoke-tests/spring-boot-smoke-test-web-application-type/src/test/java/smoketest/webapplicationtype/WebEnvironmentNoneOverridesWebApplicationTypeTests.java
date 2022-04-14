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

package smoketest.webapplicationtype;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for a web environment of none overriding the configured web
 * application type.
 *
 * @author Andy Wilkinson
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class WebEnvironmentNoneOverridesWebApplicationTypeTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void contextIsPlain() {
		assertThat(this.context).isNotInstanceOf(ReactiveWebApplicationContext.class);
		assertThat(this.context).isNotInstanceOf(WebApplicationContext.class);
	}

}
