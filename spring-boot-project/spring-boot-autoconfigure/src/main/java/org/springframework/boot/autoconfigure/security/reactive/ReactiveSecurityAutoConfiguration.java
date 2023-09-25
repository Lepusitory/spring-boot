/*
 * Copyright 2012-2023 the original author or authors.
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

package org.springframework.boot.autoconfigure.security.reactive;

import reactor.core.publisher.Flux;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Security in a reactive
 * application. Switches on {@link EnableWebFluxSecurity @EnableWebFluxSecurity} for a
 * reactive web application if this annotation has not been added by the user. It
 * delegates to Spring Security's content-negotiation mechanism for authentication. This
 * configuration also backs off if a bean of type {@link WebFilterChainProxy} has been
 * configured in any other way.
 *
 * @author Madhura Bhave
 * @since 2.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnClass({ Flux.class, EnableWebFluxSecurity.class, WebFilterChainProxy.class, WebFluxConfigurer.class })
public class ReactiveSecurityAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(WebFilterChainProxy.class)
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
	@Conditional(ReactiveAuthenticationManagerCondition.class)
	@EnableWebFluxSecurity
	static class EnableWebFluxSecurityConfiguration {

	}

	static final class ReactiveAuthenticationManagerCondition extends AnyNestedCondition {

		ReactiveAuthenticationManagerCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnBean(AuthenticationManager.class)
		static final class ConditionalOnAuthenticationManagerBean {

		}

		@ConditionalOnBean(ReactiveUserDetailsService.class)
		static final class ConditionalOnReactiveUserDetailsService {

		}

	}

}
