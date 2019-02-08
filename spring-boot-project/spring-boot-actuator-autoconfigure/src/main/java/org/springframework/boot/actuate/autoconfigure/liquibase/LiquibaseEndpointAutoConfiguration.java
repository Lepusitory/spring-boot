/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.actuate.autoconfigure.liquibase;

import liquibase.integration.spring.SpringLiquibase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnExposedEndpoint;
import org.springframework.boot.actuate.liquibase.LiquibaseEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link LiquibaseEndpoint}.
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SpringLiquibase.class)
@ConditionalOnEnabledEndpoint(endpoint = LiquibaseEndpoint.class)
@ConditionalOnExposedEndpoint(endpoint = LiquibaseEndpoint.class)
@AutoConfigureAfter(LiquibaseAutoConfiguration.class)
public class LiquibaseEndpointAutoConfiguration {

	@Bean
	@ConditionalOnBean(SpringLiquibase.class)
	@ConditionalOnMissingBean
	public LiquibaseEndpoint liquibaseEndpoint(ApplicationContext context) {
		return new LiquibaseEndpoint(context);
	}

	@Bean
	@ConditionalOnBean(SpringLiquibase.class)
	public static BeanPostProcessor preventDataSourceCloseBeanPostProcessor() {
		return new BeanPostProcessor() {

			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName)
					throws BeansException {
				if (bean instanceof DataSourceClosingSpringLiquibase) {
					((DataSourceClosingSpringLiquibase) bean)
							.setCloseDataSourceOnceMigrated(false);
				}
				return bean;
			}

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName)
					throws BeansException {
				return bean;
			}

		};
	}

}
