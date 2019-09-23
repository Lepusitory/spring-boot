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

package org.springframework.boot.test.autoconfigure.web.reactive;

import org.springframework.test.web.reactive.server.WebTestClient.Builder;

/**
 * A customizer for a {@link Builder}. Any {@code WebTestClientBuilderCustomizer} beans
 * found in the application context will be {@link #customize called} to customize the
 * auto-configured {@link Builder}.
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 * @see WebTestClientAutoConfiguration
 * @deprecated since 2.2 in favor of
 * {@link org.springframework.boot.test.web.reactive.server.WebTestClientBuilderCustomizer}
 */
@FunctionalInterface
@Deprecated
public interface WebTestClientBuilderCustomizer
		extends org.springframework.boot.test.web.reactive.server.WebTestClientBuilderCustomizer {

}
