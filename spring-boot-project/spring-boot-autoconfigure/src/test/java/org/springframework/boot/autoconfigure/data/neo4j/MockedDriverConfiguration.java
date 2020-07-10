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
package org.springframework.boot.autoconfigure.data.neo4j;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.types.TypeSystem;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Driver configuration mocked to avoid instantiation of a real driver with connection
 * creation.
 *
 * @author Michael J. Simons
 */
@Configuration(proxyBeanMethods = false)
class MockedDriverConfiguration {

	@Bean
	Driver driver() {
		Driver driver = mock(Driver.class);
		TypeSystem typeSystem = mock(TypeSystem.class);
		Session session = mock(Session.class);
		when(driver.defaultTypeSystem()).thenReturn(typeSystem);
		when(driver.session(Mockito.any(SessionConfig.class))).thenReturn(session);
		return driver;
	}

}
