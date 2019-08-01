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

package org.springframework.boot.test.autoconfigure.web.servlet.mockmvc;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * Integration tests for {@link WebMvcTest @WebMvcTest} and Spring HATEOAS.
 *
 * @author Andy Wilkinson
 */
@WebMvcTest
@WithMockUser
class WebMvcTestHateoasIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void plainResponse() throws Exception {
		this.mockMvc.perform(get("/hateoas/plain"))
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/json"));
	}

	@Test
	void hateoasResponse() throws Exception {
		this.mockMvc.perform(get("/hateoas/resource"))
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/hal+json"));
	}

}
