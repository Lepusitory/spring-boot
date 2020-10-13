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

package org.springframework.boot.context.config;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginTrackedResource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * {@link ConfigDataLoader} for {@link Resource} backed locations.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public class StandardConfigDataLoader implements ConfigDataLoader<StandardConfigDataResource> {

	@Override
	public ConfigData load(ConfigDataLoaderContext context, StandardConfigDataResource resource)
			throws IOException, ConfigDataNotFoundException {
		ConfigDataResourceNotFoundException.throwIfDoesNotExist(resource, resource.getResource());
		StandardConfigDataReference reference = resource.getReference();
		Resource originTrackedResource = OriginTrackedResource.of(resource.getResource(),
				Origin.from(reference.getConfigDataLocation()));
		String name = String.format("Config resource '%s' via location '%s'", reference.getResourceLocation(),
				reference.getConfigDataLocation());
		List<PropertySource<?>> propertySources = reference.getPropertySourceLoader().load(name, originTrackedResource);
		return new ConfigData(propertySources);
	}

}
