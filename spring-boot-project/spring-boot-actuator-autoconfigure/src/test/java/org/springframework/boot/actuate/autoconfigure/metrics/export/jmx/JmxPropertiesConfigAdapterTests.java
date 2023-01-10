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

package org.springframework.boot.actuate.autoconfigure.metrics.export.jmx;

import java.time.Duration;

import io.micrometer.jmx.JmxConfig;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.autoconfigure.metrics.export.TestConfigsToPropertiesExposure;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JmxPropertiesConfigAdapter}.
 *
 * @author Mirko Sobeck
 */
class JmxPropertiesConfigAdapterTests {

	@Test
	void whenPropertiesStepIsSetAdapterStepReturnsIt() {
		JmxProperties properties = new JmxProperties();
		properties.setStep(Duration.ofMinutes(15));
		assertThat(new JmxPropertiesConfigAdapter(properties).step()).isEqualTo(Duration.ofMinutes(15));
	}

	@Test
	void whenPropertiesDomainIsSetAdapterDomainReturnsIt() {
		JmxProperties properties = new JmxProperties();
		properties.setDomain("abc");
		assertThat(new JmxPropertiesConfigAdapter(properties).domain()).isEqualTo("abc");
	}

	@Test
	void allConfigDefaultMethodsAreOverriddenByAdapter() {
		TestConfigsToPropertiesExposure.assertThatAllConfigDefaultMethodsAreOverriddenByAdapter(JmxConfig.class,
				JmxPropertiesConfigAdapter.class);
	}

}
