/*
 * Copyright 2012-2018 the original author or authors.
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

package org.springframework.boot.test.autoconfigure.web.reactive.webclient;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Tests for {@link WebFluxTest} when a specific controller is defined.
 *
 * @author Stephane Nicoll
 */
@RunWith(SpringRunner.class)
@WithMockUser
@WebFluxTest(controllers = ExampleController1.class)
public class WebFluxTestOneControllerIntegrationTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	public void shouldFindController() {
		this.webClient.get().uri("/one").exchange().expectStatus().isOk()
				.expectBody(String.class).isEqualTo("one");
	}

	@Test
	public void shouldNotScanOtherController() {
		this.webClient.get().uri("/two").exchange().expectStatus().isNotFound();
	}

}
