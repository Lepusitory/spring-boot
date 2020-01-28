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

package org.springframework.boot.autoconfigure.mongo;

import java.util.List;

import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import org.springframework.core.env.Environment;

/**
 * A factory for a reactive {@link MongoClient} that applies {@link MongoProperties}.
 *
 * @author Mark Paluch
 * @author Stephane Nicoll
 * @author Christoph Strobl
 * @since 2.0.0
 */
public class ReactiveMongoClientFactory extends MongoClientFactorySupport<MongoClient> {

	public ReactiveMongoClientFactory(MongoProperties properties, Environment environment,
			List<MongoClientSettingsBuilderCustomizer> builderCustomizers) {
		super(properties, environment, builderCustomizers);
	}

	protected MongoClient createNetworkMongoClient(MongoClientSettings settings) {
		return MongoClients.create(settings, driverInformation());
	}

	@Override
	protected MongoClient createEmbeddedMongoClient(MongoClientSettings settings) {
		return MongoClients.create(settings, driverInformation());
	}

}
