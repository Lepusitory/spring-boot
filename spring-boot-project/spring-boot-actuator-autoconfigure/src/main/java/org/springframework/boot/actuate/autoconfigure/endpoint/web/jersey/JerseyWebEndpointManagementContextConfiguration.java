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

package org.springframework.boot.actuate.autoconfigure.endpoint.web.jersey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.jersey.JerseyEndpointResourceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.jersey.JerseyProperties;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.DefaultJerseyApplicationPath;
import org.springframework.boot.autoconfigure.web.servlet.JerseyApplicationPath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link ManagementContextConfiguration} for Jersey {@link Endpoint} concerns.
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Michael Simons
 * @author Madhura Bhave
 */
@ManagementContextConfiguration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(ResourceConfig.class)
@ConditionalOnBean(WebEndpointsSupplier.class)
@ConditionalOnMissingBean(type = "org.springframework.web.servlet.DispatcherServlet")
class JerseyWebEndpointManagementContextConfiguration {

	@Bean
	public ResourceConfigCustomizer webEndpointRegistrar(
			WebEndpointsSupplier webEndpointsSupplier,
			ServletEndpointsSupplier servletEndpointsSupplier,
			EndpointMediaTypes endpointMediaTypes,
			WebEndpointProperties webEndpointProperties) {
		List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
		allEndpoints.addAll(webEndpointsSupplier.getEndpoints());
		allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
		return (resourceConfig) -> {
			JerseyEndpointResourceFactory resourceFactory = new JerseyEndpointResourceFactory();
			String basePath = webEndpointProperties.getBasePath();
			EndpointMapping endpointMapping = new EndpointMapping(basePath);
			Collection<ExposableWebEndpoint> webEndpoints = Collections
					.unmodifiableCollection(webEndpointsSupplier.getEndpoints());
			resourceConfig.registerResources(
					new HashSet<>(resourceFactory.createEndpointResources(endpointMapping,
							webEndpoints, endpointMediaTypes,
							new EndpointLinksResolver(allEndpoints, basePath))));
		};
	}

	@Configuration
	@ConditionalOnMissingBean(ResourceConfig.class)
	@EnableConfigurationProperties(JerseyProperties.class)
	static class ResourceConfigConfiguration {

		@Bean
		public ResourceConfig resourceConfig(
				ObjectProvider<ResourceConfigCustomizer> resourceConfigCustomizers) {
			ResourceConfig resourceConfig = new ResourceConfig();
			resourceConfigCustomizers.orderedStream()
					.forEach((customizer) -> customizer.customize(resourceConfig));
			return resourceConfig;
		}

		@Bean
		@ConditionalOnMissingBean
		public JerseyApplicationPath jerseyApplicationPath(JerseyProperties properties,
				ResourceConfig config) {
			return new DefaultJerseyApplicationPath(properties.getApplicationPath(),
					config);
		}

		@Bean
		public ServletRegistrationBean<ServletContainer> jerseyServletRegistration(
				ObjectProvider<ResourceConfigCustomizer> resourceConfigCustomizers,
				JerseyApplicationPath jerseyApplicationPath) {
			return new ServletRegistrationBean<>(
					new ServletContainer(resourceConfig(resourceConfigCustomizers)),
					jerseyApplicationPath.getUrlMapping());
		}

	}

}
