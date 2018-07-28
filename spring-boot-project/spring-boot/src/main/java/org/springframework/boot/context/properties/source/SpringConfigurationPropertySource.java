/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.properties.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.PropertySourceOrigin;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.util.Assert;

/**
 * {@link ConfigurationPropertySource} backed by a non-enumerable Spring
 * {@link PropertySource} or a restricted {@link EnumerablePropertySource} implementation
 * (such as a security restricted {@code systemEnvironment} source). A
 * {@link PropertySource} is adapted with the help of a {@link PropertyMapper} which
 * provides the mapping rules for individual properties.
 * <p>
 * Each {@link ConfigurationPropertySource#getConfigurationProperty
 * getConfigurationProperty} call attempts to
 * {@link PropertyMapper#map(ConfigurationPropertyName) map} the
 * {@link ConfigurationPropertyName} to one or more {@code String} based names. This
 * allows fast property resolution for well formed property sources.
 * <p>
 * When possible the {@link SpringIterableConfigurationPropertySource} will be used in
 * preference to this implementation since it supports full "relaxed" style resolution.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @see #from(PropertySource)
 * @see PropertyMapper
 * @see SpringIterableConfigurationPropertySource
 */
class SpringConfigurationPropertySource implements ConfigurationPropertySource {

	private static final ConfigurationPropertyName RANDOM = ConfigurationPropertyName
			.of("random");

	private final PropertySource<?> propertySource;

	private final PropertyMapper mapper;

	private final Function<ConfigurationPropertyName, ConfigurationPropertyState> containsDescendantOf;

	/**
	 * Create a new {@link SpringConfigurationPropertySource} implementation.
	 * @param propertySource the source property source
	 * @param mapper the property mapper
	 * @param containsDescendantOf function used to implement
	 * {@link #containsDescendantOf(ConfigurationPropertyName)} (may be {@code null})
	 */
	SpringConfigurationPropertySource(PropertySource<?> propertySource,
			PropertyMapper mapper,
			Function<ConfigurationPropertyName, ConfigurationPropertyState> containsDescendantOf) {
		Assert.notNull(propertySource, "PropertySource must not be null");
		Assert.notNull(mapper, "Mapper must not be null");
		this.propertySource = propertySource;
		this.mapper = (mapper instanceof DelegatingPropertyMapper) ? mapper
				: new DelegatingPropertyMapper(mapper);
		this.containsDescendantOf = (containsDescendantOf != null) ? containsDescendantOf
				: (n) -> ConfigurationPropertyState.UNKNOWN;
	}

	@Override
	public ConfigurationProperty getConfigurationProperty(
			ConfigurationPropertyName name) {
		PropertyMapping[] mappings = getMapper().map(name);
		return find(mappings, name);
	}

	@Override
	public ConfigurationPropertyState containsDescendantOf(
			ConfigurationPropertyName name) {
		return this.containsDescendantOf.apply(name);
	}

	@Override
	public Object getUnderlyingSource() {
		return this.propertySource;
	}

	protected final ConfigurationProperty find(PropertyMapping[] mappings,
			ConfigurationPropertyName name) {
		for (PropertyMapping candidate : mappings) {
			if (candidate.isApplicable(name)) {
				ConfigurationProperty result = find(candidate);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private ConfigurationProperty find(PropertyMapping mapping) {
		String propertySourceName = mapping.getPropertySourceName();
		Object value = getPropertySource().getProperty(propertySourceName);
		if (value == null) {
			return null;
		}
		ConfigurationPropertyName configurationPropertyName = mapping
				.getConfigurationPropertyName();
		Origin origin = PropertySourceOrigin.get(this.propertySource, propertySourceName);
		return ConfigurationProperty.of(configurationPropertyName, value, origin);
	}

	protected PropertySource<?> getPropertySource() {
		return this.propertySource;
	}

	protected final PropertyMapper getMapper() {
		return this.mapper;
	}

	@Override
	public String toString() {
		return this.propertySource.toString();
	}

	/**
	 * Create a new {@link SpringConfigurationPropertySource} for the specified
	 * {@link PropertySource}.
	 * @param source the source Spring {@link PropertySource}
	 * @return a {@link SpringConfigurationPropertySource} or
	 * {@link SpringIterableConfigurationPropertySource} instance
	 */
	public static SpringConfigurationPropertySource from(PropertySource<?> source) {
		Assert.notNull(source, "Source must not be null");
		PropertyMapper mapper = getPropertyMapper(source);
		if (isFullEnumerable(source)) {
			return new SpringIterableConfigurationPropertySource(
					(EnumerablePropertySource<?>) source, mapper);
		}
		return new SpringConfigurationPropertySource(source, mapper,
				getContainsDescendantOfForSource(source));
	}

	private static PropertyMapper getPropertyMapper(PropertySource<?> source) {
		if (source instanceof SystemEnvironmentPropertySource
				&& hasSystemEnvironmentName(source)) {
			return new DelegatingPropertyMapper(SystemEnvironmentPropertyMapper.INSTANCE,
					DefaultPropertyMapper.INSTANCE);
		}
		return new DelegatingPropertyMapper(DefaultPropertyMapper.INSTANCE);
	}

	private static boolean hasSystemEnvironmentName(PropertySource<?> source) {
		String name = source.getName();
		return StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME.equals(name)
				|| name.endsWith("-"
						+ StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
	}

	private static boolean isFullEnumerable(PropertySource<?> source) {
		PropertySource<?> rootSource = getRootSource(source);
		if (rootSource.getSource() instanceof Map) {
			// Check we're not security restricted
			try {
				((Map<?, ?>) rootSource.getSource()).size();
			}
			catch (UnsupportedOperationException ex) {
				return false;
			}
		}
		return (source instanceof EnumerablePropertySource);
	}

	private static PropertySource<?> getRootSource(PropertySource<?> source) {
		while (source.getSource() != null
				&& source.getSource() instanceof PropertySource) {
			source = (PropertySource<?>) source.getSource();
		}
		return source;
	}

	private static Function<ConfigurationPropertyName, ConfigurationPropertyState> getContainsDescendantOfForSource(
			PropertySource<?> source) {
		if (source.getSource() instanceof Random) {
			return SpringConfigurationPropertySource::containsDescendantOfForRandom;
		}
		return null;
	}

	private static ConfigurationPropertyState containsDescendantOfForRandom(
			ConfigurationPropertyName name) {
		if (name.isAncestorOf(RANDOM) || name.equals(RANDOM)) {
			return ConfigurationPropertyState.PRESENT;
		}
		return ConfigurationPropertyState.ABSENT;
	}

	/**
	 * {@link PropertyMapper} that delegates to other {@link PropertyMapper}s and also
	 * swallows exceptions when the mapping fails.
	 */
	private static class DelegatingPropertyMapper implements PropertyMapper {

		private final PropertyMapper[] mappers;

		DelegatingPropertyMapper(PropertyMapper... mappers) {
			this.mappers = mappers;
		}

		@Override
		public PropertyMapping[] map(
				ConfigurationPropertyName configurationPropertyName) {
			return callMappers((mapper) -> mapper.map(configurationPropertyName));
		}

		@Override
		public PropertyMapping[] map(String propertySourceName) {
			return callMappers((mapper) -> mapper.map(propertySourceName));
		}

		private PropertyMapping[] callMappers(
				Function<PropertyMapper, PropertyMapping[]> function) {
			List<PropertyMapping> mappings = new ArrayList<>();
			for (PropertyMapper mapper : this.mappers) {
				try {
					mappings.addAll(Arrays.asList(function.apply(mapper)));
				}
				catch (Exception ex) {
				}
			}
			return mappings.toArray(new PropertyMapping[] {});
		}

	}

}
