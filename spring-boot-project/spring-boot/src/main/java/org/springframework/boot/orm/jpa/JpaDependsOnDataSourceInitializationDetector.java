/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.boot.orm.jpa;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.springframework.boot.jdbc.init.AbstractBeansOfTypeDependsOnDataSourceInitializationDetector;
import org.springframework.boot.jdbc.init.DependsOnDataSourceInitializationDetector;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;

/**
 * {@link DependsOnDataSourceInitializationDetector} for JPA.
 *
 * @author Andy Wilkinson
 */
class JpaDependsOnDataSourceInitializationDetector
		extends AbstractBeansOfTypeDependsOnDataSourceInitializationDetector {

	private final Environment environment;

	JpaDependsOnDataSourceInitializationDetector(Environment environment) {
		this.environment = environment;
	}

	@Override
	protected Set<Class<?>> getDependsOnDataSourceInitializationBeanTypes() {
		boolean postpone = this.environment.getProperty("spring.jpa.defer-datasource-initialization", boolean.class,
				false);
		return postpone ? Collections.emptySet()
				: new HashSet<>(Arrays.asList(EntityManagerFactory.class, AbstractEntityManagerFactoryBean.class));
	}

}
