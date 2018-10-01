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

package org.springframework.boot.actuate.autoconfigure.endpoint;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ExposeExcludePropertyEndpointFilter}.
 *
 * @author Phillip Webb
 */
public class ExposeExcludePropertyEndpointFilterTests {

	private ExposeExcludePropertyEndpointFilter<?> filter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createWhenEndpointTypeIsNullShouldThrowException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ExposeExcludePropertyEndpointFilter<>(null,
						new MockEnvironment(), "foo"))
				.withMessageContaining("EndpointType must not be null");
	}

	@Test
	public void createWhenEnvironmentIsNullShouldThrowException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ExposeExcludePropertyEndpointFilter<>(
						ExposableEndpoint.class, null, "foo"))
				.withMessageContaining("Environment must not be null");
	}

	@Test
	public void createWhenPrefixIsNullShouldThrowException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ExposeExcludePropertyEndpointFilter<>(
						ExposableEndpoint.class, new MockEnvironment(), null))
				.withMessageContaining("Prefix must not be empty");
	}

	@Test
	public void createWhenPrefixIsEmptyShouldThrowException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ExposeExcludePropertyEndpointFilter<>(
						ExposableEndpoint.class, new MockEnvironment(), ""))
				.withMessageContaining("Prefix must not be empty");
	}

	@Test
	public void matchWhenExposeIsEmptyAndExcludeIsEmptyAndInDefaultShouldMatch() {
		setupFilter("", "");
		assertThat(match("def")).isTrue();
	}

	@Test
	public void matchWhenExposeIsEmptyAndExcludeIsEmptyAndNotInDefaultShouldNotMatch() {
		setupFilter("", "");
		assertThat(match("bar")).isFalse();
	}

	@Test
	public void matchWhenExposeMatchesAndExcludeIsEmptyShouldMatch() {
		setupFilter("bar", "");
		assertThat(match("bar")).isTrue();
	}

	@Test
	public void matchWhenExposeDoesNotMatchAndExcludeIsEmptyShouldNotMatch() {
		setupFilter("bar", "");
		assertThat(match("baz")).isFalse();
	}

	@Test
	public void matchWhenExposeMatchesAndExcludeMatchesShouldNotMatch() {
		setupFilter("bar,baz", "baz");
		assertThat(match("baz")).isFalse();
	}

	@Test
	public void matchWhenExposeMatchesAndExcludeDoesNotMatchShouldMatch() {
		setupFilter("bar,baz", "buz");
		assertThat(match("baz")).isTrue();
	}

	@Test
	public void matchWhenExposeMatchesWithDifferentCaseShouldMatch() {
		setupFilter("bar", "");
		assertThat(match("bAr")).isTrue();
	}

	@Test
	public void matchWhenDiscovererDoesNotMatchShouldMatch() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("foo.include", "bar");
		environment.setProperty("foo.exclude", "");
		this.filter = new ExposeExcludePropertyEndpointFilter<>(
				DifferentTestExposableWebEndpoint.class, environment, "foo");
		assertThat(match("baz")).isTrue();
	}

	@Test
	public void matchWhenIncludeIsAsteriskShouldMatchAll() {
		setupFilter("*", "buz");
		assertThat(match("bar")).isTrue();
		assertThat(match("baz")).isTrue();
		assertThat(match("buz")).isFalse();
	}

	@Test
	public void matchWhenExcludeIsAsteriskShouldMatchNone() {
		setupFilter("bar,baz,buz", "*");
		assertThat(match("bar")).isFalse();
		assertThat(match("baz")).isFalse();
		assertThat(match("buz")).isFalse();
	}

	private void setupFilter(String include, String exclude) {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("foo.include", include);
		environment.setProperty("foo.exclude", exclude);
		this.filter = new ExposeExcludePropertyEndpointFilter<>(
				TestExposableWebEndpoint.class, environment, "foo", "def");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean match(String id) {
		ExposableEndpoint<?> endpoint = mock(TestExposableWebEndpoint.class);
		given(endpoint.getId()).willReturn(id);
		return ((EndpointFilter) this.filter).match(endpoint);
	}

	private abstract static class TestExposableWebEndpoint
			implements ExposableWebEndpoint {

	}

	private abstract static class DifferentTestExposableWebEndpoint
			implements ExposableWebEndpoint {

	}

}
