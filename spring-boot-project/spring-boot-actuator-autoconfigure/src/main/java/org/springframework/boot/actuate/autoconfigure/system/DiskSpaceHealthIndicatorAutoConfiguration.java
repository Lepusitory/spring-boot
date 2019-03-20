/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.actuate.autoconfigure.system;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.system.DiskSpaceHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for
 * {@link DiskSpaceHealthIndicator}.
 *
 * @author Mattias Severson
 * @author Andy Wilkinson
 * @since 2.0.0
 */
@Configuration
@ConditionalOnEnabledHealthIndicator("diskspace")
@AutoConfigureBefore(HealthIndicatorAutoConfiguration.class)
public class DiskSpaceHealthIndicatorAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(name = "diskSpaceHealthIndicator")
	public DiskSpaceHealthIndicator diskSpaceHealthIndicator(
			DiskSpaceHealthIndicatorProperties properties) {
		return new DiskSpaceHealthIndicator(properties.getPath(),
				properties.getThreshold());
	}

	@Bean
	public DiskSpaceHealthIndicatorProperties diskSpaceHealthIndicatorProperties() {
		return new DiskSpaceHealthIndicatorProperties();
	}

}
