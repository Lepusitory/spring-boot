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
package org.springframework.boot.autoconfigure.security.oauth2.resource.servlet;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOAuth2TokenIntrospectionClient;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2TokenIntrospectionClient;

/**
 * Configures a {@link OAuth2TokenIntrospectionClient} when a token introspection endpoint
 * is available. Also configures a {@link WebSecurityConfigurerAdapter} if a
 * {@link OAuth2TokenIntrospectionClient} bean is found.
 *
 * @author Madhura Bhave
 */
@Configuration(proxyBeanMethods = false)
class OAuth2ResourceServerOpaqueTokenConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(OAuth2TokenIntrospectionClient.class)
	static class OpaqueTokenIntrospectionClientConfiguration {

		@Bean
		@ConditionalOnProperty(name = "spring.security.oauth2.resourceserver.opaque-token.introspection-uri")
		public NimbusOAuth2TokenIntrospectionClient oAuth2TokenIntrospectionClient(
				OAuth2ResourceServerProperties properties) {
			OAuth2ResourceServerProperties.OpaqueToken opaqueToken = properties.getOpaqueToken();
			return new NimbusOAuth2TokenIntrospectionClient(opaqueToken.getIntrospectionUri(),
					opaqueToken.getClientId(), opaqueToken.getClientSecret());
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
	static class OAuth2WebSecurityConfigurerAdapter {

		@Bean
		@ConditionalOnBean(OAuth2TokenIntrospectionClient.class)
		public WebSecurityConfigurerAdapter opaqueTokenWebSecurityConfigurerAdapter() {
			return new WebSecurityConfigurerAdapter() {
				@Override
				protected void configure(HttpSecurity http) throws Exception {
					http.authorizeRequests().anyRequest().authenticated().and().oauth2ResourceServer().opaqueToken();
				}
			};
		}

	}

}
