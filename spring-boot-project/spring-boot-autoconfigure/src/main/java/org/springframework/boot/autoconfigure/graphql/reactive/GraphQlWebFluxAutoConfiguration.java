/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.boot.autoconfigure.graphql.reactive;

import java.util.Collections;
import java.util.stream.Collectors;

import graphql.GraphQL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.graphql.GraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.graphql.web.WebGraphQlHandler;
import org.springframework.graphql.web.WebInterceptor;
import org.springframework.graphql.web.webflux.GraphQlHttpHandler;
import org.springframework.graphql.web.webflux.SchemaHandler;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for enabling Spring GraphQL over
 * WebFlux.
 *
 * @author Brian Clozel
 * @since 2.7.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({ GraphQL.class, GraphQlHttpHandler.class })
@ConditionalOnBean(GraphQlService.class)
@AutoConfigureAfter(GraphQlAutoConfiguration.class)
public class GraphQlWebFluxAutoConfiguration {

	private static final Log logger = LogFactory.getLog(GraphQlWebFluxAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean
	public GraphQlHttpHandler graphQlHttpHandler(WebGraphQlHandler webGraphQlHandler) {
		return new GraphQlHttpHandler(webGraphQlHandler);
	}

	@Bean
	@ConditionalOnMissingBean
	public WebGraphQlHandler webGraphQlHandler(GraphQlService service,
			ObjectProvider<WebInterceptor> interceptorsProvider) {
		return WebGraphQlHandler.builder(service)
				.interceptors(interceptorsProvider.orderedStream().collect(Collectors.toList())).build();
	}

	@Bean
	public RouterFunction<ServerResponse> graphQlEndpoint(GraphQlHttpHandler handler, GraphQlSource graphQlSource,
			GraphQlProperties properties, ResourceLoader resourceLoader) {

		String graphQLPath = properties.getPath();
		if (logger.isInfoEnabled()) {
			logger.info("GraphQL endpoint HTTP POST " + graphQLPath);
		}

		RouterFunctions.Builder builder = RouterFunctions.route()
				.GET(graphQLPath,
						(request) -> ServerResponse.status(HttpStatus.METHOD_NOT_ALLOWED)
								.headers((headers) -> headers.setAllow(Collections.singleton(HttpMethod.POST))).build())
				.POST(graphQLPath, accept(MediaType.APPLICATION_JSON).and(contentType(MediaType.APPLICATION_JSON)),
						handler::handleRequest);

		if (properties.getSchema().getPrinter().isEnabled()) {
			SchemaHandler schemaHandler = new SchemaHandler(graphQlSource);
			builder = builder.GET(graphQLPath + "/schema", schemaHandler::handleRequest);
		}

		return builder.build();
	}

}
