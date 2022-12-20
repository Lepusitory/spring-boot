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

package smoketest.jetty.web;

import jakarta.servlet.http.HttpServletResponse;
import smoketest.jetty.service.HelloWorldService;
import smoketest.jetty.service.HttpHeaderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SampleController {

	@Autowired
	private HelloWorldService helloWorldService;

	@Autowired
	private HttpHeaderService httpHeaderService;

	@GetMapping("/")
	@ResponseBody
	public String helloWorld() {
		return this.helloWorldService.getHelloMessage();
	}

	@GetMapping("/max-http-response-header")
	@ResponseBody
	public String maxHttpResponseHeader(HttpServletResponse response) {
		String headerValue = httpHeaderService.getHeaderValue();
		response.addHeader("x-max-header", headerValue);
		return this.helloWorldService.getHelloMessage();
	}

}
