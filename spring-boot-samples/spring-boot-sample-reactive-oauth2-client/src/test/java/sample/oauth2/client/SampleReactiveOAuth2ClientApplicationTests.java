/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.oauth2.client;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"APP-CLIENT-ID=my-client-id", "APP-CLIENT-SECRET=my-client-secret",
		"YAHOO-CLIENT-ID=my-google-client-id",
		"YAHOO-CLIENT-SECRET=my-google-client-secret" })
public class SampleReactiveOAuth2ClientApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void everythingShouldRedirectToLogin() {
		this.webTestClient.get().uri("/").exchange().expectStatus().isFound()
				.expectHeader().valueEquals("Location", "/login");
	}

	@Test
	public void loginShouldHaveBothOAuthClientsToChooseFrom() {
		byte[] body = this.webTestClient.get().uri("/login").exchange().expectStatus()
				.isOk().returnResult(String.class).getResponseBodyContent();
		String bodyString = new String(body);
		assertThat(bodyString).contains("/oauth2/authorization/yahoo");
		assertThat(bodyString).contains("/oauth2/authorization/github-client-1");
		assertThat(bodyString).contains("/oauth2/authorization/github-client-2");
	}

}
