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

package org.springframework.boot.autoconfigure.data.jpa;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data's JPA Repositories.
 * <p>
 * Activates when there is a bean of type {@link javax.sql.DataSource} configured in the
 * context, the Spring Data JPA
 * {@link org.springframework.data.jpa.repository.JpaRepository} type is on the classpath,
 * and there is no other, existing
 * {@link org.springframework.data.jpa.repository.JpaRepository} configured.
 * <p>
 * Once in effect, the auto-configuration is the equivalent of enabling JPA repositories
 * using the {@link org.springframework.data.jpa.repository.config.EnableJpaRepositories}
 * annotation.
 * <p>
 * This configuration class will activate <em>after</em> the Hibernate auto-configuration.
 *
 * @author Phillip Webb
 * @author Josh Long
 * @see EnableJpaRepositories
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@ConditionalOnClass(JpaRepository.class)
@ConditionalOnMissingBean({ JpaRepositoryFactoryBean.class,
		JpaRepositoryConfigExtension.class })
@ConditionalOnProperty(prefix = "spring.data.jpa.repositories", name = "enabled",
		havingValue = "true", matchIfMissing = true)
@Import(JpaRepositoriesAutoConfigureRegistrar.class)
@AutoConfigureAfter({ HibernateJpaAutoConfiguration.class,
		TaskExecutionAutoConfiguration.class })
public class JpaRepositoriesAutoConfiguration {

	@Bean
	@Conditional(BootstrapExecutorCondition.class)
	public EntityManagerFactoryBuilderCustomizer entityManagerFactoryBootstrapExecutorCustomizer(
			Map<String, AsyncTaskExecutor> taskExecutors) {
		return (builder) -> {
			AsyncTaskExecutor bootstrapExecutor = determineBootstrapExecutor(
					taskExecutors);
			if (bootstrapExecutor != null) {
				builder.setBootstrapExecutor(bootstrapExecutor);
			}
		};
	}

	private AsyncTaskExecutor determineBootstrapExecutor(
			Map<String, AsyncTaskExecutor> taskExecutors) {
		if (taskExecutors.size() == 1) {
			return taskExecutors.values().iterator().next();
		}
		return taskExecutors
				.get(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);
	}

	private static final class BootstrapExecutorCondition extends AnyNestedCondition {

		BootstrapExecutorCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnProperty(prefix = "spring.data.jpa.repositories",
				name = "bootstrap-mode", havingValue = "deferred", matchIfMissing = false)
		static class DeferredBootstrapMode {

		}

		@ConditionalOnProperty(prefix = "spring.data.jpa.repositories",
				name = "bootstrap-mode", havingValue = "lazy", matchIfMissing = false)
		static class LazyBootstrapMode {

		}

	}

}
