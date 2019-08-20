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

package org.springframework.boot.actuate.health;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link HealthEndpoint}.
 *
 * @author Phillip Webb
 */
class HealthEndpointTests
		extends HealthEndpointSupportTests<HealthContributorRegistry, HealthContributor, HealthComponent> {

	@Test
	@SuppressWarnings("deprecation")
	void createWhenUsingDeprecatedConstructorThrowsException() {
		HealthIndicator healthIndicator = mock(HealthIndicator.class);
		assertThatIllegalStateException().isThrownBy(() -> new HealthEndpoint(healthIndicator))
				.withMessage("Unable to create class org.springframework.boot.actuate.health.HealthEndpoint "
						+ "using deprecated constructor");
	}

	@Test
	void healthReturnsCompositeHealth() {
		this.registry.registerContributor("test", createContributor(this.up));
		HealthComponent health = create(this.registry, this.settings).health();
		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health).isInstanceOf(CompositeHealth.class);
	}

	@Test
	void healthWhenPathDoesNotExistReturnsNull() {
		this.registry.registerContributor("test", createContributor(this.up));
		HealthComponent health = create(this.registry, this.settings).healthForPath("missing");
		assertThat(health).isNull();
	}

	@Test
	void healthWhenPathExistsReturnsHealth() {
		this.registry.registerContributor("test", createContributor(this.up));
		HealthComponent health = create(this.registry, this.settings).healthForPath("test");
		assertThat(health).isEqualTo(this.up);
	}

	@Override
	protected HealthEndpoint create(HealthContributorRegistry registry, HealthEndpointSettings settings) {
		return new HealthEndpoint(registry, settings);
	}

	@Override
	protected HealthContributorRegistry createRegistry() {
		return new DefaultHealthContributorRegistry();
	}

	@Override
	protected HealthContributor createContributor(Health health) {
		return (HealthIndicator) () -> health;
	}

	@Override
	protected HealthContributor createCompositeContributor(Map<String, HealthContributor> contributors) {
		return CompositeHealthContributor.fromMap(contributors);
	}

	@Override
	protected HealthComponent getHealth(HealthComponent result) {
		return result;
	}

}
