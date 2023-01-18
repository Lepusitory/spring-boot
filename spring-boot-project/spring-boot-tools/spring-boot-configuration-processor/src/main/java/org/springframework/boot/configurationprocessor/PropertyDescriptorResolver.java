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

package org.springframework.boot.configurationprocessor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

/**
 * Resolve {@link PropertyDescriptor} instances.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 */
class PropertyDescriptorResolver {

	private final MetadataGenerationEnvironment environment;

	PropertyDescriptorResolver(MetadataGenerationEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * Return the {@link PropertyDescriptor} instances that are valid candidates for the
	 * specified {@link TypeElement type} based on the specified {@link ExecutableElement
	 * factory method}, if any.
	 * @param type the target type
	 * @param factoryMethod the method that triggered the metadata for that {@code type}
	 * or {@code null}
	 * @return the candidate properties for metadata generation
	 */
	Stream<PropertyDescriptor<?>> resolve(TypeElement type, ExecutableElement factoryMethod) {
		TypeElementMembers members = new TypeElementMembers(this.environment, type);
		if (factoryMethod != null) {
			return resolveJavaBeanProperties(type, factoryMethod, members);
		}
		return resolve(ConfigurationPropertiesTypeElement.of(type, this.environment), members);
	}

	private Stream<PropertyDescriptor<?>> resolve(ConfigurationPropertiesTypeElement type, TypeElementMembers members) {
		if (type.isConstructorBindingEnabled()) {
			ExecutableElement constructor = type.getBindConstructor();
			if (constructor != null) {
				return resolveConstructorProperties(type.getType(), members, constructor);
			}
			return Stream.empty();
		}
		return resolveJavaBeanProperties(type.getType(), null, members);
	}

	Stream<PropertyDescriptor<?>> resolveConstructorProperties(TypeElement type, TypeElementMembers members,
			ExecutableElement constructor) {
		Map<String, PropertyDescriptor<?>> candidates = new LinkedHashMap<>();
		constructor.getParameters().forEach((parameter) -> {
			String name = getParameterName(parameter);
			TypeMirror propertyType = parameter.asType();
			ExecutableElement getter = members.getPublicGetter(name, propertyType);
			ExecutableElement setter = members.getPublicSetter(name, propertyType);
			VariableElement field = members.getFields().get(name);
			register(candidates, new ConstructorParameterPropertyDescriptor(type, null, parameter, name, propertyType,
					field, getter, setter));
		});
		return candidates.values().stream();
	}

	private String getParameterName(VariableElement parameter) {
		AnnotationMirror nameAnnotation = this.environment.getNameAnnotation(parameter);
		if (nameAnnotation != null) {
			return (String) this.environment.getAnnotationElementValues(nameAnnotation).get("value");
		}
		return parameter.getSimpleName().toString();
	}

	Stream<PropertyDescriptor<?>> resolveJavaBeanProperties(TypeElement type, ExecutableElement factoryMethod,
			TypeElementMembers members) {
		// First check if we have regular java bean properties there
		Map<String, PropertyDescriptor<?>> candidates = new LinkedHashMap<>();
		members.getPublicGetters().forEach((name, getters) -> {
			VariableElement field = members.getFields().get(name);
			ExecutableElement getter = findMatchingGetter(members, getters, field);
			TypeMirror propertyType = getter.getReturnType();
			register(candidates, new JavaBeanPropertyDescriptor(type, factoryMethod, getter, name, propertyType, field,
					members.getPublicSetter(name, propertyType)));
		});
		// Then check for Lombok ones
		members.getFields().forEach((name, field) -> {
			TypeMirror propertyType = field.asType();
			ExecutableElement getter = members.getPublicGetter(name, propertyType);
			ExecutableElement setter = members.getPublicSetter(name, propertyType);
			register(candidates,
					new LombokPropertyDescriptor(type, factoryMethod, field, name, propertyType, getter, setter));
		});
		return candidates.values().stream();
	}

	private ExecutableElement findMatchingGetter(TypeElementMembers members, List<ExecutableElement> candidates,
			VariableElement field) {
		if (candidates.size() > 1 && field != null) {
			return members.getMatchingGetter(candidates, field.asType());
		}
		return candidates.get(0);
	}

	private void register(Map<String, PropertyDescriptor<?>> candidates, PropertyDescriptor<?> descriptor) {
		if (!candidates.containsKey(descriptor.getName()) && isCandidate(descriptor)) {
			candidates.put(descriptor.getName(), descriptor);
		}
	}

	private boolean isCandidate(PropertyDescriptor<?> descriptor) {
		return descriptor.isProperty(this.environment) || descriptor.isNested(this.environment);
	}

	/**
	 * Wrapper around a {@link TypeElement} that could be bound.
	 */
	private static class ConfigurationPropertiesTypeElement {

		private final TypeElement type;

		private final List<ExecutableElement> constructors;

		private final List<ExecutableElement> boundConstructors;

		ConfigurationPropertiesTypeElement(TypeElement type, List<ExecutableElement> constructors,
				List<ExecutableElement> boundConstructors) {
			this.type = type;
			this.constructors = constructors;
			this.boundConstructors = boundConstructors;
		}

		TypeElement getType() {
			return this.type;
		}

		boolean isConstructorBindingEnabled() {
			return !this.boundConstructors.isEmpty();
		}

		ExecutableElement getBindConstructor() {
			if (this.boundConstructors.isEmpty()) {
				return findBoundConstructor();
			}
			if (this.boundConstructors.size() == 1) {
				return this.boundConstructors.get(0);
			}
			return null;
		}

		private ExecutableElement findBoundConstructor() {
			ExecutableElement boundConstructor = null;
			for (ExecutableElement candidate : this.constructors) {
				if (!candidate.getParameters().isEmpty()) {
					if (boundConstructor != null) {
						return null;
					}
					boundConstructor = candidate;
				}
			}
			return boundConstructor;
		}

		static ConfigurationPropertiesTypeElement of(TypeElement type, MetadataGenerationEnvironment env) {
			List<ExecutableElement> constructors = ElementFilter.constructorsIn(type.getEnclosedElements());
			List<ExecutableElement> boundConstructors = getBoundConstructors(type, env, constructors);
			return new ConfigurationPropertiesTypeElement(type, constructors, boundConstructors);
		}

		private static List<ExecutableElement> getBoundConstructors(TypeElement type, MetadataGenerationEnvironment env,
				List<ExecutableElement> constructors) {
			ExecutableElement bindConstructor = deduceBindConstructor(type, constructors, env);
			if (bindConstructor != null) {
				return Collections.singletonList(bindConstructor);
			}
			return constructors.stream().filter(env::hasConstructorBindingAnnotation).toList();
		}

		private static ExecutableElement deduceBindConstructor(TypeElement type, List<ExecutableElement> constructors,
				MetadataGenerationEnvironment env) {
			if (constructors.size() == 1) {
				ExecutableElement candidate = constructors.get(0);
				if (candidate.getParameters().size() > 0 && !env.hasAutowiredAnnotation(candidate)) {
					if (type.getNestingKind() == NestingKind.MEMBER
							&& candidate.getModifiers().contains(Modifier.PRIVATE)) {
						return null;
					}
					return candidate;
				}
			}
			return null;
		}

	}

}
