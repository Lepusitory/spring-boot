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

package org.springframework.boot.build.context.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuration properties read from one or more
 * {@code META-INF/spring-configuration-metadata.json} files.
 *
 * @author Andy Wilkinson
 */
final class ConfigurationProperties {

	private ConfigurationProperties() {

	}

	@SuppressWarnings("unchecked")
	static Map<String, ConfigurationProperty> fromFiles(Iterable<File> files) {
		List<ConfigurationProperty> configurationProperties = new ArrayList<>();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			for (File file : files) {
				try (Reader reader = new FileReader(file)) {
					Map<String, Object> json = objectMapper.readValue(file, Map.class);
					List<Map<String, Object>> properties = (List<Map<String, Object>>) json.get("properties");
					for (Map<String, Object> property : properties) {
						String name = (String) property.get("name");
						String type = (String) property.get("type");
						Object defaultValue = property.get("defaultValue");
						String description = (String) property.get("description");
						boolean deprecated = property.containsKey("deprecated");
						configurationProperties
								.add(new ConfigurationProperty(name, type, defaultValue, description, deprecated));
					}
				}
			}
			return configurationProperties.stream()
					.collect(Collectors.toMap(ConfigurationProperty::getName, Function.identity()));
		}
		catch (IOException ex) {
			throw new RuntimeException("Failed to load configuration metadata", ex);
		}
	}

}
