/*
 * Copyright 2012-2022 the original author or authors.
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

package org.springframework.boot.actuate.endpoint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.boot.actuate.endpoint.Producible;

/**
 * Identifies a method on an {@link Endpoint @Endpoint} as being a write operation.
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Reflective(OperationReflectiveProcessor.class)
public @interface WriteOperation {

	/**
	 * The media type of the result of the operation.
	 * @return the media type
	 */
	String[] produces() default {};

	/**
	 * The media types of the result of the operation.
	 * @return the media types
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Producible> producesFrom() default Producible.class;

}
