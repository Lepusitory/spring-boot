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

package org.springframework.boot.logging.logback;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.SyslogStartConverter;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;
import org.slf4j.bridge.SLF4JBridgeHandler;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeHint.Builder;
import org.springframework.aot.hint.TypeReference;
import org.springframework.util.ClassUtils;

/**
 * {@link RuntimeHintsRegistrar} for Logback.
 *
 * @author Andy Wilkinson
 */
class LogbackRuntimeHints implements RuntimeHintsRegistrar {

	private static final Consumer<Builder> DEFAULT_HINT = (hint) -> {
	};

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		if (!ClassUtils.isPresent("ch.qos.logback.classic.LoggerContext", classLoader)) {
			return;
		}
		ReflectionHints reflection = hints.reflection();
		registerHintsForLogbackLoggingSystemTypeChecks(reflection, classLoader);
		registerHintsForBuiltInLogbackConverters(reflection);
		registerHintsForSpringBootConverters(reflection);
	}

	private void registerHintsForLogbackLoggingSystemTypeChecks(ReflectionHints reflection, ClassLoader classLoader) {
		reflection.registerType(LoggerContext.class, DEFAULT_HINT);
		if (ClassUtils.isPresent("org.slf4j.bridge.SLF4JBridgeHandler", classLoader)) {
			reflection.registerType(SLF4JBridgeHandler.class, DEFAULT_HINT);
		}
	}

	private void registerHintsForBuiltInLogbackConverters(ReflectionHints reflection) {
		registerForPublicConstructorInvocation(reflection, DateTokenConverter.class, IntegerTokenConverter.class,
				SyslogStartConverter.class);
	}

	private void registerHintsForSpringBootConverters(ReflectionHints reflection) {
		registerForPublicConstructorInvocation(reflection, ColorConverter.class,
				ExtendedWhitespaceThrowableProxyConverter.class, WhitespaceThrowableProxyConverter.class);
	}

	private void registerForPublicConstructorInvocation(ReflectionHints reflection, Class<?>... classes) {
		reflection.registerTypes(typeReferences(classes),
				(hint) -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
	}

	private Iterable<TypeReference> typeReferences(Class<?>... classes) {
		return Stream.of(classes).map(TypeReference::of).collect(Collectors.toList());
	}

}
