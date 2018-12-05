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

package org.springframework.boot.autoconfigure.jdbc;

import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.jndi.JndiPropertiesHidingClassLoader;
import org.springframework.boot.autoconfigure.jndi.TestableInitialContextFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link JndiDataSourceAutoConfiguration}
 *
 * @author Andy Wilkinson
 */
public class JndiDataSourceAutoConfigurationTests {

	private ClassLoader threadContextClassLoader;

	private String initialContextFactory;

	private AnnotationConfigApplicationContext context;

	@BeforeEach
	public void setupJndi() {
		this.initialContextFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				TestableInitialContextFactory.class.getName());
	}

	@BeforeEach
	public void setupThreadContextClassLoader() {
		this.threadContextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(
				new JndiPropertiesHidingClassLoader(getClass().getClassLoader()));
	}

	@AfterEach
	public void close() {
		TestableInitialContextFactory.clearAll();
		if (this.initialContextFactory != null) {
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
					this.initialContextFactory);
		}
		else {
			System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
		}
		if (this.context != null) {
			this.context.close();
		}
		Thread.currentThread().setContextClassLoader(this.threadContextClassLoader);
	}

	@Test
	public void dataSourceIsAvailableFromJndi()
			throws IllegalStateException, NamingException {
		DataSource dataSource = new BasicDataSource();
		configureJndi("foo", dataSource);

		this.context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.datasource.jndi-name:foo").applyTo(this.context);
		this.context.register(JndiDataSourceAutoConfiguration.class);
		this.context.refresh();

		assertThat(this.context.getBean(DataSource.class)).isEqualTo(dataSource);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void mbeanDataSourceIsExcludedFromExport()
			throws IllegalStateException, NamingException {
		DataSource dataSource = new BasicDataSource();
		configureJndi("foo", dataSource);

		this.context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.datasource.jndi-name:foo").applyTo(this.context);
		this.context.register(JndiDataSourceAutoConfiguration.class,
				MBeanExporterConfiguration.class);
		this.context.refresh();

		assertThat(this.context.getBean(DataSource.class)).isEqualTo(dataSource);
		MBeanExporter exporter = this.context.getBean(MBeanExporter.class);
		Set<String> excludedBeans = (Set<String>) ReflectionTestUtils.getField(exporter,
				"excludedBeans");
		assertThat(excludedBeans).containsExactly("dataSource");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void mbeanDataSourceIsExcludedFromExportByAllExporters()
			throws IllegalStateException, NamingException {
		DataSource dataSource = new BasicDataSource();
		configureJndi("foo", dataSource);
		this.context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.datasource.jndi-name:foo").applyTo(this.context);
		this.context.register(JndiDataSourceAutoConfiguration.class,
				MBeanExporterConfiguration.class,
				AnotherMBeanExporterConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBean(DataSource.class)).isEqualTo(dataSource);
		for (MBeanExporter exporter : this.context.getBeansOfType(MBeanExporter.class)
				.values()) {
			Set<String> excludedBeans = (Set<String>) ReflectionTestUtils
					.getField(exporter, "excludedBeans");
			assertThat(excludedBeans).containsExactly("dataSource");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void standardDataSourceIsNotExcludedFromExport()
			throws IllegalStateException, NamingException {
		DataSource dataSource = mock(DataSource.class);
		configureJndi("foo", dataSource);

		this.context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.datasource.jndi-name:foo").applyTo(this.context);
		this.context.register(JndiDataSourceAutoConfiguration.class,
				MBeanExporterConfiguration.class);
		this.context.refresh();

		assertThat(this.context.getBean(DataSource.class)).isEqualTo(dataSource);
		MBeanExporter exporter = this.context.getBean(MBeanExporter.class);
		Set<String> excludedBeans = (Set<String>) ReflectionTestUtils.getField(exporter,
				"excludedBeans");
		assertThat(excludedBeans).isEmpty();
	}

	private void configureJndi(String name, DataSource dataSource)
			throws IllegalStateException {
		TestableInitialContextFactory.bind(name, dataSource);
	}

	private static class MBeanExporterConfiguration {

		@Bean
		MBeanExporter mbeanExporter() {
			return new MBeanExporter();
		}

	}

	private static class AnotherMBeanExporterConfiguration {

		@Bean
		MBeanExporter anotherMbeanExporter() {
			return new MBeanExporter();
		}

	}

}
