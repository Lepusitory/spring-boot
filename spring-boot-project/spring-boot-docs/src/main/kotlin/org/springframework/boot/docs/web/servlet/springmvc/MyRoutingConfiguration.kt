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

package org.springframework.boot.docs.web.servlet.springmvc

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.RequestPredicates.accept
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.RouterFunctions
import org.springframework.web.servlet.function.ServerResponse

@Configuration(proxyBeanMethods = false)
class MyRoutingConfiguration {

	@Bean
	fun routerFunction(userHandler: MyUserHandler): RouterFunction<ServerResponse> {
		return RouterFunctions.route()
			.GET("/{user}", ACCEPT_JSON, userHandler::getUser)
			.GET("/{user}/customers", ACCEPT_JSON, userHandler::getUserCustomers)
			.DELETE("/{user}", ACCEPT_JSON, userHandler::deleteUser)
			.build()
	}

	companion object {
		private val ACCEPT_JSON = accept(MediaType.APPLICATION_JSON)
	}

}
