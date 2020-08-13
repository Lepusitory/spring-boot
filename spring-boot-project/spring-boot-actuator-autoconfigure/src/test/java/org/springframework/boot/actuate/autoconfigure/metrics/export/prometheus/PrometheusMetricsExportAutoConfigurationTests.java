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

package org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus;

import java.util.function.Consumer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.BasicAuthHttpConnectionFactory;
import io.prometheus.client.exporter.DefaultHttpConnectionFactory;
import io.prometheus.client.exporter.HttpConnectionFactory;
import io.prometheus.client.exporter.PushGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusPushGatewayManager;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PrometheusMetricsExportAutoConfiguration}.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
@ExtendWith(OutputCaptureExtension.class)
class PrometheusMetricsExportAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(PrometheusMetricsExportAutoConfiguration.class));

	@Test
	void backsOffWithoutAClock() {
		this.contextRunner.run((context) -> assertThat(context).doesNotHaveBean(PrometheusMeterRegistry.class));
	}

	@Test
	void autoConfiguresItsConfigCollectorRegistryAndMeterRegistry() {
		this.contextRunner.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> assertThat(context).hasSingleBean(PrometheusMeterRegistry.class)
						.hasSingleBean(CollectorRegistry.class).hasSingleBean(PrometheusConfig.class));
	}

	@Test
	void autoConfigurationCanBeDisabledWithDefaultsEnabledProperty() {
		this.contextRunner.withUserConfiguration(BaseConfiguration.class)
				.withPropertyValues("management.metrics.export.defaults.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean(PrometheusMeterRegistry.class)
						.doesNotHaveBean(CollectorRegistry.class).doesNotHaveBean(PrometheusConfig.class));
	}

	@Test
	void autoConfigurationCanBeDisabledWithSpecificEnabledProperty() {
		this.contextRunner.withUserConfiguration(BaseConfiguration.class)
				.withPropertyValues("management.metrics.export.prometheus.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean(PrometheusMeterRegistry.class)
						.doesNotHaveBean(CollectorRegistry.class).doesNotHaveBean(PrometheusConfig.class));
	}

	@Test
	void allowsCustomConfigToBeUsed() {
		this.contextRunner.withUserConfiguration(CustomConfigConfiguration.class)
				.run((context) -> assertThat(context).hasSingleBean(PrometheusMeterRegistry.class)
						.hasSingleBean(CollectorRegistry.class).hasSingleBean(PrometheusConfig.class)
						.hasBean("customConfig"));
	}

	@Test
	void allowsCustomRegistryToBeUsed() {
		this.contextRunner.withUserConfiguration(CustomRegistryConfiguration.class)
				.run((context) -> assertThat(context).hasSingleBean(PrometheusMeterRegistry.class)
						.hasBean("customRegistry").hasSingleBean(CollectorRegistry.class)
						.hasSingleBean(PrometheusConfig.class));
	}

	@Test
	void allowsCustomCollectorRegistryToBeUsed() {
		this.contextRunner.withUserConfiguration(CustomCollectorRegistryConfiguration.class)
				.run((context) -> assertThat(context).hasSingleBean(PrometheusMeterRegistry.class)
						.hasBean("customCollectorRegistry").hasSingleBean(CollectorRegistry.class)
						.hasSingleBean(PrometheusConfig.class));
	}

	@Test
	void addsScrapeEndpointToManagementContext() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withUserConfiguration(BaseConfiguration.class)
				.withPropertyValues("management.endpoints.web.exposure.include=prometheus")
				.run((context) -> assertThat(context).hasSingleBean(PrometheusScrapeEndpoint.class));
	}

	@Test
	void scrapeEndpointNotAddedToManagementContextWhenNotExposed() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> assertThat(context).doesNotHaveBean(PrometheusScrapeEndpoint.class));
	}

	@Test
	void scrapeEndpointCanBeDisabled() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues("management.endpoints.web.exposure.include=prometheus")
				.withPropertyValues("management.endpoint.prometheus.enabled=false")
				.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> assertThat(context).doesNotHaveBean(PrometheusScrapeEndpoint.class));
	}

	@Test
	void allowsCustomScrapeEndpointToBeUsed() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withUserConfiguration(CustomEndpointConfiguration.class).run((context) -> assertThat(context)
						.hasBean("customEndpoint").hasSingleBean(PrometheusScrapeEndpoint.class));
	}

	@Test
	void pushGatewayIsNotConfiguredWhenEnabledFlagIsNotSet() {
		this.contextRunner.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> assertThat(context).doesNotHaveBean(PrometheusPushGatewayManager.class));
	}

	@Test
	void withPushGatewayEnabled(CapturedOutput output) {
		this.contextRunner.withConfiguration(AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues("management.metrics.export.prometheus.pushgateway.enabled=true")
				.withUserConfiguration(BaseConfiguration.class).run((context) -> {
					assertThat(output).doesNotContain("Invalid PushGateway base url");
					hasGatewayURL(context, "http://localhost:9091/metrics/");
				});
	}

	@Test
	void withPushGatewayNoBasicAuth() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues("management.metrics.export.prometheus.pushgateway.enabled=true")
				.withUserConfiguration(BaseConfiguration.class)
				.run(hasHttpConnectionFactory((httpConnectionFactory) -> assertThat(httpConnectionFactory)
						.isInstanceOf(DefaultHttpConnectionFactory.class)));
	}

	@Test
	@Deprecated
	void withCustomLegacyPushGatewayURL(CapturedOutput output) {
		this.contextRunner.withConfiguration(AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues("management.metrics.export.prometheus.pushgateway.enabled=true",
						"management.metrics.export.prometheus.pushgateway.base-url=localhost:9090")
				.withUserConfiguration(BaseConfiguration.class).run((context) -> {
					assertThat(output).contains("Invalid PushGateway base url").contains("localhost:9090");
					hasGatewayURL(context, "http://localhost:9090/metrics/");
				});
	}

	@Test
	void withCustomPushGatewayURL() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues("management.metrics.export.prometheus.pushgateway.enabled=true",
						"management.metrics.export.prometheus.pushgateway.base-url=https://example.com:8080")
				.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> hasGatewayURL(context, "https://example.com:8080/metrics/"));
	}

	@Test
	void withPushGatewayBasicAuth() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues("management.metrics.export.prometheus.pushgateway.enabled=true",
						"management.metrics.export.prometheus.pushgateway.username=admin",
						"management.metrics.export.prometheus.pushgateway.password=secret")
				.withUserConfiguration(BaseConfiguration.class)
				.run(hasHttpConnectionFactory((httpConnectionFactory) -> assertThat(httpConnectionFactory)
						.isInstanceOf(BasicAuthHttpConnectionFactory.class)));
	}

	private void hasGatewayURL(AssertableApplicationContext context, String url) {
		assertThat(getPushGateway(context)).hasFieldOrPropertyWithValue("gatewayBaseURL", url);
	}

	private ContextConsumer<AssertableApplicationContext> hasHttpConnectionFactory(
			Consumer<HttpConnectionFactory> httpConnectionFactory) {
		return (context) -> {
			PushGateway pushGateway = getPushGateway(context);
			httpConnectionFactory
					.accept((HttpConnectionFactory) ReflectionTestUtils.getField(pushGateway, "connectionFactory"));
		};
	}

	private PushGateway getPushGateway(AssertableApplicationContext context) {
		assertThat(context).hasSingleBean(PrometheusPushGatewayManager.class);
		PrometheusPushGatewayManager gatewayManager = context.getBean(PrometheusPushGatewayManager.class);
		return (PushGateway) ReflectionTestUtils.getField(gatewayManager, "pushGateway");
	}

	@Configuration(proxyBeanMethods = false)
	static class BaseConfiguration {

		@Bean
		Clock clock() {
			return Clock.SYSTEM;
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Import(BaseConfiguration.class)
	static class CustomConfigConfiguration {

		@Bean
		PrometheusConfig customConfig() {
			return (key) -> null;
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Import(BaseConfiguration.class)
	static class CustomRegistryConfiguration {

		@Bean
		PrometheusMeterRegistry customRegistry(PrometheusConfig config, CollectorRegistry collectorRegistry,
				Clock clock) {
			return new PrometheusMeterRegistry(config, collectorRegistry, clock);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Import(BaseConfiguration.class)
	static class CustomCollectorRegistryConfiguration {

		@Bean
		CollectorRegistry customCollectorRegistry() {
			return new CollectorRegistry();
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Import(BaseConfiguration.class)
	static class CustomEndpointConfiguration {

		@Bean
		PrometheusScrapeEndpoint customEndpoint(CollectorRegistry collectorRegistry) {
			return new PrometheusScrapeEndpoint(collectorRegistry);
		}

	}

}
