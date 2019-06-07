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

package org.springframework.boot.actuate.metrics.cache;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.cache.EhCache2Metrics;

import org.springframework.cache.ehcache.EhCacheCache;

/**
 * {@link CacheMeterBinderProvider} implementation for EhCache2.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public class EhCache2CacheMeterBinderProvider implements CacheMeterBinderProvider<EhCacheCache> {

	@Override
	public MeterBinder getMeterBinder(EhCacheCache cache, Iterable<Tag> tags) {
		return new EhCache2Metrics(cache.getNativeCache(), tags);
	}

}
