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

package org.springframework.boot.actuate.autoconfigure.metrics.export.stackdriver;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * {@link ConfigurationProperties @ConfigurationProperties} for configuring Stackdriver metrics
 * export.
 *
 * @author Johannes Graf
 */
@ConfigurationProperties(prefix = "management.metrics.export.stackdriver")
public class StackdriverProperties extends StepRegistryProperties {

	/**
	 * The ID of your google cloud platform project
	 */
	private String projectId;

	/**
	 * The resource type of the metrics
	 */
	private String resourceType = "global";

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
}
