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

package org.springframework.boot.docs.actuator.metrics.export.graphite

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.config.NamingConvention
import io.micrometer.core.instrument.util.HierarchicalNameMapper
import io.micrometer.graphite.GraphiteConfig
import io.micrometer.graphite.GraphiteMeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class MyGraphiteConfiguration {
	@Bean
	fun graphiteMeterRegistry(config: GraphiteConfig, clock: Clock): GraphiteMeterRegistry {
		return GraphiteMeterRegistry(config, clock,
			HierarchicalNameMapper { id: Meter.Id, convention: NamingConvention ->
				toHierarchicalName(
					id,
					convention
				)
			})
	}

	private fun toHierarchicalName(id: Meter.Id, convention: NamingConvention): String {
		return  /**/HierarchicalNameMapper.DEFAULT.toHierarchicalName(id, convention)
	}
}