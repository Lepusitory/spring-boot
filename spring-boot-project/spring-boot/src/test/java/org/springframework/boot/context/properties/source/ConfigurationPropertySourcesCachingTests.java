/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.context.properties.source;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for {@link ConfigurationPropertySourcesCaching}.
 *
 * @author Phillip Webb
 */
class ConfigurationPropertySourcesCachingTests {

	private List<ConfigurationPropertySource> sources;

	private ConfigurationPropertySourcesCaching caching;

	@BeforeEach
	void setup() {
		this.sources = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			this.sources.add(mockSource(i % 2 == 0));
		}
		this.caching = new ConfigurationPropertySourcesCaching(this.sources);
	}

	private ConfigurationPropertySource mockSource(boolean cachingSource) {
		if (!cachingSource) {
			return mock(ConfigurationPropertySource.class);
		}
		ConfigurationPropertySource source = mock(ConfigurationPropertySource.class,
				withSettings().extraInterfaces(CachingConfigurationPropertySource.class));
		ConfigurationPropertyCaching caching = mock(ConfigurationPropertyCaching.class);
		given(((CachingConfigurationPropertySource) source).getCaching()).willReturn(caching);
		return source;
	}

	@Test
	void enableDelegatesToCachingConfigurationPropertySources() {
		this.caching.enable();
		verify(getCaching(0)).enable();
		verify(getCaching(2)).enable();
	}

	@Test
	void enableWhenSourcesIsNullDoesNothing() {
		new ConfigurationPropertySourcesCaching(null).enable();
	}

	@Test
	void disableDelegatesToCachingConfigurationPropertySources() {
		this.caching.disable();
		verify(getCaching(0)).disable();
		verify(getCaching(2)).disable();
	}

	@Test
	void disableWhenSourcesIsNullDoesNothing() {
		new ConfigurationPropertySourcesCaching(null).disable();
	}

	@Test
	void setTimeToLiveDelegatesToCachingConfigurationPropertySources() {
		Duration ttl = Duration.ofDays(1);
		this.caching.setTimeToLive(ttl);
		verify(getCaching(0)).setTimeToLive(ttl);
		verify(getCaching(2)).setTimeToLive(ttl);
	}

	@Test
	void setTimeToLiveWhenSourcesIsNullDoesNothing() {
		new ConfigurationPropertySourcesCaching(null).setTimeToLive(Duration.ofSeconds(1));
	}

	@Test
	void clearDelegatesToCachingConfigurationPropertySources() {
		this.caching.clear();
		verify(getCaching(0)).clear();
		verify(getCaching(2)).clear();
	}

	@Test
	void clearWhenSourcesIsNullDoesNothing() {
		new ConfigurationPropertySourcesCaching(null).enable();
	}

	private ConfigurationPropertyCaching getCaching(int index) {
		return CachingConfigurationPropertySource.find(this.sources.get(index));
	}

}
