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

package org.springframework.boot.actuate.health;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HealthContributorNameFactory}.
 *
 * @author Phillip Webb
 */
class HealthContributorNameFactoryTests {

	@Test
	void applyWhenNameDoesNotEndWithSuffixReturnsName() {
		assertThat(HealthContributorNameFactory.INSTANCE.apply("test")).isEqualTo("test");
	}

	@Test
	void applyWhenNameEndsWithSuffixReturnsNewName() {
		assertThat(HealthContributorNameFactory.INSTANCE.apply("testHealthIndicator")).isEqualTo("test");
		assertThat(HealthContributorNameFactory.INSTANCE.apply("testHealthContributor")).isEqualTo("test");
	}

	@Test
	void applyWhenNameEndsWithSuffixInDifferentReturnsNewName() {
		assertThat(HealthContributorNameFactory.INSTANCE.apply("testHEALTHindicator")).isEqualTo("test");
		assertThat(HealthContributorNameFactory.INSTANCE.apply("testHEALTHcontributor")).isEqualTo("test");
	}

	@Test
	void applyWhenNameContainsSuffixReturnsName() {
		assertThat(HealthContributorNameFactory.INSTANCE.apply("testHealthIndicatorTest"))
			.isEqualTo("testHealthIndicatorTest");
		assertThat(HealthContributorNameFactory.INSTANCE.apply("testHealthContributorTest"))
			.isEqualTo("testHealthContributorTest");
	}

}
