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

package org.springframework.boot.autoconfigure.influx;

import okhttp3.OkHttpClient;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.impl.InfluxDBImpl;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for InfluxDB.
 *
 * @author Sergey Kuptsov
 * @author Stephane Nicoll
 * @author Eddú Meléndez
 * @since 2.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(InfluxDB.class)
@EnableConfigurationProperties(InfluxDbProperties.class)
public class InfluxDbAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty("spring.influx.url")
	public InfluxDB influxDb(InfluxDbProperties properties, ObjectProvider<InfluxDbOkHttpClientBuilderProvider> builder,
			ObjectProvider<InfluxDbCustomizer> customizers) {
		InfluxDB influxDb = new InfluxDBImpl(properties.getUrl(), properties.getUser(), properties.getPassword(),
				determineBuilder(builder.getIfAvailable()));
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		map.from(properties::getConsistency).to(influxDb::setConsistency);
		map.from(properties::getDatabase).to(influxDb::setDatabase);
		map.from(properties::getLog).to(influxDb::setLogLevel);
		map.from(properties::getRetentionPolicy).to(influxDb::setRetentionPolicy);
		map.from(properties.isGzipEnabled()).whenTrue().toCall(influxDb::enableGzip);
		if (properties.getBatch().isEnabled()) {
			BatchOptions batchOptions = mapBatchOptions(properties);
			influxDb.enableBatch(batchOptions);
		}
		customizers.orderedStream().forEach((customizer) -> customizer.customize(influxDb));
		return influxDb;
	}

	private BatchOptions mapBatchOptions(InfluxDbProperties properties) {
		InfluxDbProperties.Batch batch = properties.getBatch();
		return BatchOptions.DEFAULTS.actions(batch.getActions())
				.flushDuration(Long.valueOf(batch.getFlushDuration().toMillis()).intValue())
				.jitterDuration(Long.valueOf(batch.getJitterDuration().toMillis()).intValue())
				.bufferLimit(batch.getBufferLimit()).consistency(batch.getConsistency()).precision(batch.getPrecision())
				.dropActionsOnQueueExhaustion(batch.isDropActionsOnQueueExhaustion());
	}

	private static OkHttpClient.Builder determineBuilder(InfluxDbOkHttpClientBuilderProvider builder) {
		if (builder != null) {
			return builder.get();
		}
		return new OkHttpClient.Builder();
	}

}
