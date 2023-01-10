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

package org.springframework.boot.actuate.autoconfigure.metrics.export.influx;

import io.micrometer.influx.InfluxApiVersion;
import io.micrometer.influx.InfluxConfig;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.autoconfigure.metrics.export.TestConfigsToPropertiesExposure;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InfluxPropertiesConfigAdapter}.
 *
 * @author Stephane Nicoll
 */
class InfluxPropertiesConfigAdapterTests {

	@Test
	void adaptInfluxV1BasicConfig() {
		InfluxProperties properties = new InfluxProperties();
		properties.setDb("test-db");
		properties.setUri("https://influx.example.com:8086");
		properties.setUserName("user");
		properties.setPassword("secret");
		InfluxPropertiesConfigAdapter adapter = new InfluxPropertiesConfigAdapter(properties);
		assertThat(adapter.apiVersion()).isEqualTo(InfluxApiVersion.V1);
		assertThat(adapter.db()).isEqualTo("test-db");
		assertThat(adapter.uri()).isEqualTo("https://influx.example.com:8086");
		assertThat(adapter.userName()).isEqualTo("user");
		assertThat(adapter.password()).isEqualTo("secret");
	}

	@Test
	void adaptInfluxV2BasicConfig() {
		InfluxProperties properties = new InfluxProperties();
		properties.setOrg("test-org");
		properties.setBucket("test-bucket");
		properties.setUri("https://influx.example.com:8086");
		properties.setToken("token");
		InfluxPropertiesConfigAdapter adapter = new InfluxPropertiesConfigAdapter(properties);
		assertThat(adapter.apiVersion()).isEqualTo(InfluxApiVersion.V2);
		assertThat(adapter.org()).isEqualTo("test-org");
		assertThat(adapter.bucket()).isEqualTo("test-bucket");
		assertThat(adapter.uri()).isEqualTo("https://influx.example.com:8086");
		assertThat(adapter.token()).isEqualTo("token");
	}

	@Test
	void allConfigDefaultMethodsAreOverriddenByAdapter() {
		TestConfigsToPropertiesExposure.assertThatAllConfigDefaultMethodsAreOverriddenByAdapter(InfluxConfig.class,
				InfluxPropertiesConfigAdapter.class);
	}

}
