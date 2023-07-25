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

package org.springframework.boot.actuate.autoconfigure.opentelemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for OpenTelemetry.
 *
 * @author Moritz Halbritter
 * @since 3.2.0
 */
@ConfigurationProperties(prefix = "management.opentelemetry")
public class OpenTelemetryProperties {

	/**
	 * Resource attributes.
	 */
	private Map<String, String> resourceAttributes = new HashMap<>();

	public Map<String, String> getResourceAttributes() {
		return this.resourceAttributes;
	}

	public void setResourceAttributes(Map<String, String> resourceAttributes) {
		this.resourceAttributes = resourceAttributes;
	}

	Resource toResource() {
		ResourceBuilder builder = Resource.builder();
		for (Entry<String, String> entry : this.resourceAttributes.entrySet()) {
			builder.put(entry.getKey(), entry.getValue());
		}
		return builder.build();
	}

}
