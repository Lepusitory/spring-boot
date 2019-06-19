/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.test.autoconfigure.webservices.client;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.ws.test.client.RequestMatchers;
import org.springframework.ws.test.client.ResponseCreators;
import org.springframework.xml.transform.StringSource;

/**
 * Tests for {@link AutoConfigureWebServiceClient @AutoConfigureWebServiceClient} with
 * {@code registerWebServiceTemplate=true}.
 *
 * @author Dmytro Nosan
 */
@SpringBootTest
@AutoConfigureWebServiceClient(registerWebServiceTemplate = true)
@AutoConfigureMockWebServiceServer
class AutoConfigureWebServiceClientWebServiceTemplateIntegrationTests {

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Autowired
	private MockWebServiceServer mockWebServiceServer;

	@Test
	void webServiceTemplateTest() {
		this.mockWebServiceServer.expect(RequestMatchers.payload(new StringSource("<request/>")))
				.andRespond(ResponseCreators.withPayload(new StringSource("<response/>")));
		this.webServiceTemplate.marshalSendAndReceive("https://example.com", new Request());
	}

	@Configuration(proxyBeanMethods = false)
	@Import(WebServiceMarshallerConfiguration.class)
	static class Config {

	}

}
