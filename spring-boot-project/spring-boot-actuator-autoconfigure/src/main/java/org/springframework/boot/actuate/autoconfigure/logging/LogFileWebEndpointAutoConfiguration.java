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

package org.springframework.boot.actuate.autoconfigure.logging;

import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.logging.LogFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link LogFileWebEndpoint}.
 *
 * @author Andy Wilkinson
 * @author Christian Carriere-Tisseur
 * @since 2.0.0
 */
@Configuration
@EnableConfigurationProperties(LogFileWebEndpointProperties.class)
public class LogFileWebEndpointAutoConfiguration {

	private final LogFileWebEndpointProperties properties;

	public LogFileWebEndpointAutoConfiguration(LogFileWebEndpointProperties properties) {
		this.properties = properties;
	}

	@Bean
	@ConditionalOnMissingBean
	@Conditional(LogFileCondition.class)
	public LogFileWebEndpoint logFileWebEndpoint(Environment environment) {
		return new LogFileWebEndpoint(environment, this.properties.getExternalFile());
	}

	private static class LogFileCondition extends SpringBootCondition {

		@SuppressWarnings("deprecation")
		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context,
				AnnotatedTypeMetadata metadata) {
			Environment environment = context.getEnvironment();
			String config = getLogFileConfig(environment, LogFile.FILE_NAME_PROPERTY,
					LogFile.FILE_PROPERTY);
			ConditionMessage.Builder message = ConditionMessage.forCondition("Log File");
			if (StringUtils.hasText(config)) {
				return ConditionOutcome
						.match(message.found(LogFile.FILE_NAME_PROPERTY).items(config));
			}
			config = getLogFileConfig(environment, LogFile.FILE_PATH_PROPERTY,
					LogFile.PATH_PROPERTY);
			if (StringUtils.hasText(config)) {
				return ConditionOutcome
						.match(message.found(LogFile.FILE_PATH_PROPERTY).items(config));
			}
			config = environment.getProperty("management.endpoint.logfile.external-file");
			if (StringUtils.hasText(config)) {
				return ConditionOutcome
						.match(message.found("management.endpoint.logfile.external-file")
								.items(config));
			}
			return ConditionOutcome.noMatch(message.didNotFind("logging file").atAll());
		}

		private String getLogFileConfig(Environment environment, String configName,
				String deprecatedConfigName) {
			String config = environment.resolvePlaceholders("${" + configName + ":}");
			if (StringUtils.hasText(config)) {
				return config;
			}
			return environment.resolvePlaceholders("${" + deprecatedConfigName + ":}");
		}

	}

}
