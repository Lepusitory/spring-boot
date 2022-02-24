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

package org.springframework.boot.actuate.autoconfigure.metrics;

import io.micrometer.binder.jvm.ClassLoaderMetrics;
import io.micrometer.binder.jvm.JvmGcMetrics;
import io.micrometer.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.binder.jvm.JvmMemoryMetrics;
import io.micrometer.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for JVM metrics.
 *
 * @author Stephane Nicoll
 * @since 2.1.0
 */
@AutoConfiguration(after = { MetricsAutoConfiguration.class, CompositeMeterRegistryAutoConfiguration.class })
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean(MeterRegistry.class)
public class JvmMetricsAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean({ JvmGcMetrics.class, io.micrometer.core.instrument.binder.jvm.JvmGcMetrics.class })
	public JvmGcMetrics jvmGcMetrics() {
		return new JvmGcMetrics();
	}

	@Bean
	@ConditionalOnMissingBean({ JvmHeapPressureMetrics.class,
			io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics.class })
	public JvmHeapPressureMetrics jvmHeapPressureMetrics() {
		return new JvmHeapPressureMetrics();
	}

	@Bean
	@ConditionalOnMissingBean({ JvmMemoryMetrics.class,
			io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics.class })
	public JvmMemoryMetrics jvmMemoryMetrics() {
		return new JvmMemoryMetrics();
	}

	@Bean
	@ConditionalOnMissingBean({ JvmThreadMetrics.class,
			io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics.class })
	public JvmThreadMetrics jvmThreadMetrics() {
		return new JvmThreadMetrics();
	}

	@Bean
	@ConditionalOnMissingBean({ ClassLoaderMetrics.class,
			io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics.class })
	public ClassLoaderMetrics classLoaderMetrics() {
		return new ClassLoaderMetrics();
	}

}
