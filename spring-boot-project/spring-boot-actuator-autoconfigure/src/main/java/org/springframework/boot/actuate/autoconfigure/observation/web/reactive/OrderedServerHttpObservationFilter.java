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

package org.springframework.boot.actuate.autoconfigure.observation.web.reactive;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.observation.ServerRequestObservationConvention;
import org.springframework.web.filter.reactive.ServerHttpObservationFilter;

/**
 * {@link ServerHttpObservationFilter} that also implements {@link Ordered}.
 *
 * @author Moritz Halbritter
 */
class OrderedServerHttpObservationFilter extends ServerHttpObservationFilter implements OrderedWebFilter {

	private final int order;

	OrderedServerHttpObservationFilter(ObservationRegistry observationRegistry,
			ServerRequestObservationConvention observationConvention, int order) {
		super(observationRegistry, observationConvention);
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

}
