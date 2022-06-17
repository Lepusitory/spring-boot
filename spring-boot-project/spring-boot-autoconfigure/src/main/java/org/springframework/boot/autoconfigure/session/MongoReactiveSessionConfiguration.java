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

package org.springframework.boot.autoconfigure.session;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.data.mongo.ReactiveMongoSessionRepository;
import org.springframework.session.data.mongo.config.annotation.web.reactive.ReactiveMongoWebSessionConfiguration;

/**
 * Mongo-backed reactive session configuration.
 *
 * @author Andy Wilkinson
 * @author Weix Sun
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ ReactiveMongoOperations.class, ReactiveMongoSessionRepository.class })
@ConditionalOnMissingBean(ReactiveSessionRepository.class)
@ConditionalOnBean(ReactiveMongoOperations.class)
@EnableConfigurationProperties(MongoSessionProperties.class)
class MongoReactiveSessionConfiguration {

	@Configuration(proxyBeanMethods = false)
	static class SpringBootReactiveMongoWebSessionConfiguration extends ReactiveMongoWebSessionConfiguration {

		@Autowired
		void customize(SessionProperties sessionProperties, MongoSessionProperties mongoSessionProperties,
				ServerProperties serverProperties) {
			Duration timeout = sessionProperties
					.determineTimeout(() -> serverProperties.getReactive().getSession().getTimeout());
			if (timeout != null) {
				setMaxInactiveIntervalInSeconds((int) timeout.getSeconds());
			}
			setCollectionName(mongoSessionProperties.getCollectionName());
		}

	}

}
