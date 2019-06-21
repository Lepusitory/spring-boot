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

package org.springframework.boot.autoconfigure.cache;

import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;

/**
 * Callback interface that can be used to customize a {@link RedisCacheManagerBuilder}.
 *
 * @author Dmytro Nosan
 * @since 2.2.0
 */
@FunctionalInterface
public interface RedisCacheManagerBuilderCustomizer {

	/**
	 * Customize the {@link RedisCacheManagerBuilder}.
	 * @param builder the builder to customize
	 */
	void customize(RedisCacheManagerBuilder builder);

}
