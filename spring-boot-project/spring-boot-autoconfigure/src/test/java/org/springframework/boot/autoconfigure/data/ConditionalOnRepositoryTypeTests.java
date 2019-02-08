/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.autoconfigure.data;

import org.junit.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConditionalOnRepositoryType}.
 *
 * @author Andy Wilkinson
 */
public class ConditionalOnRepositoryTypeTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

	@Test
	public void imperativeRepositoryMatchesWithNoConfiguredType() {
		this.contextRunner.withUserConfiguration(ImperativeRepository.class)
				.run((context) -> assertThat(context)
						.hasSingleBean(ImperativeRepository.class));
	}

	@Test
	public void reactiveRepositoryMatchesWithNoConfiguredType() {
		this.contextRunner.withUserConfiguration(ReactiveRepository.class).run(
				(context) -> assertThat(context).hasSingleBean(ReactiveRepository.class));
	}

	@Test
	public void imperativeRepositoryMatchesWithAutoConfiguredType() {
		this.contextRunner.withUserConfiguration(ImperativeRepository.class)
				.withPropertyValues("spring.data.test.repositories.type:auto")
				.run((context) -> assertThat(context)
						.hasSingleBean(ImperativeRepository.class));
	}

	@Test
	public void reactiveRepositoryMatchesWithAutoConfiguredType() {
		this.contextRunner.withUserConfiguration(ReactiveRepository.class)
				.withPropertyValues("spring.data.test.repositories.type:auto")
				.run((context) -> assertThat(context)
						.hasSingleBean(ReactiveRepository.class));
	}

	@Test
	public void imperativeRepositoryMatchesWithImperativeConfiguredType() {
		this.contextRunner.withUserConfiguration(ImperativeRepository.class)
				.withPropertyValues("spring.data.test.repositories.type:imperative")
				.run((context) -> assertThat(context)
						.hasSingleBean(ImperativeRepository.class));
	}

	@Test
	public void reactiveRepositoryMatchesWithReactiveConfiguredType() {
		this.contextRunner.withUserConfiguration(ReactiveRepository.class)
				.withPropertyValues("spring.data.test.repositories.type:reactive")
				.run((context) -> assertThat(context)
						.hasSingleBean(ReactiveRepository.class));
	}

	@Test
	public void imperativeRepositoryDoesNotMatchWithReactiveConfiguredType() {
		this.contextRunner.withUserConfiguration(ImperativeRepository.class)
				.withPropertyValues("spring.data.test.repositories.type:reactive")
				.run((context) -> assertThat(context)
						.doesNotHaveBean(ImperativeRepository.class));
	}

	@Test
	public void reactiveRepositoryDoesNotMatchWithImperativeConfiguredType() {
		this.contextRunner.withUserConfiguration(ReactiveRepository.class)
				.withPropertyValues("spring.data.test.repositories.type:imperative")
				.run((context) -> assertThat(context)
						.doesNotHaveBean(ReactiveRepository.class));
	}

	@Test
	public void imperativeRepositoryDoesNotMatchWithNoneConfiguredType() {
		this.contextRunner.withUserConfiguration(ImperativeRepository.class)
				.withPropertyValues("spring.data.test.repositories.type:none")
				.run((context) -> assertThat(context)
						.doesNotHaveBean(ImperativeRepository.class));
	}

	@Test
	public void reactiveRepositoryDoesNotMatchWithNoneConfiguredType() {
		this.contextRunner.withUserConfiguration(ReactiveRepository.class)
				.withPropertyValues("spring.data.test.repositories.type:none")
				.run((context) -> assertThat(context)
						.doesNotHaveBean(ReactiveRepository.class));
	}

	@Test
	public void failsFastWhenConfiguredTypeIsUnknown() {
		this.contextRunner.withUserConfiguration(ReactiveRepository.class)
				.withPropertyValues("spring.data.test.repositories.type:abcde")
				.run((context) -> assertThat(context).hasFailed());
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnRepositoryType(store = "test", type = RepositoryType.IMPERATIVE)
	protected static class ImperativeRepository {

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnRepositoryType(store = "test", type = RepositoryType.REACTIVE)
	protected static class ReactiveRepository {

	}

}
