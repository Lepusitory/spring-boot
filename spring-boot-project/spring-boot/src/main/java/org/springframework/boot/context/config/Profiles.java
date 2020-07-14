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

package org.springframework.boot.context.config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Provides access to environment profiles that have either been set directly on the
 * {@link Environment} or will be set based on configuration data property values.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public class Profiles implements Iterable<String> {

	private static final Set<String> UNSET_ACTIVE = Collections.emptySet();

	private static final Set<String> UNSET_DEFAULT = Collections.singleton("default");

	private final List<String> activeProfiles;

	private final List<String> defaultProfiles;

	private final List<String> acceptedProfiles;

	/**
	 * Create a new {@link Profiles} instance based on the {@link Environment} and
	 * {@link Binder}.
	 * @param environment the source environment
	 * @param binder the binder for profile properties
	 * @param additionalProfiles and additional active profiles
	 */
	Profiles(Environment environment, Binder binder, Collection<String> additionalProfiles) {
		this.activeProfiles = asUniqueItemList(get(environment, binder, environment::getActiveProfiles,
				AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, UNSET_ACTIVE), additionalProfiles);
		this.defaultProfiles = asUniqueItemList(get(environment, binder, environment::getDefaultProfiles,
				AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME, UNSET_DEFAULT));
		this.acceptedProfiles = expandAcceptedProfiles(this.activeProfiles, this.defaultProfiles);
	}

	private String[] get(Environment environment, Binder binder, Supplier<String[]> supplier, String propertyName,
			Set<String> unset) {
		String propertyValue = environment.getProperty(propertyName);
		if (hasExplicit(supplier, propertyValue, unset)) {
			return supplier.get();
		}
		return binder.bind(propertyName, String[].class).orElse(StringUtils.toStringArray(unset));
	}

	private boolean hasExplicit(Supplier<String[]> supplier, String propertyValue, Set<String> unset) {
		Set<String> profiles = new LinkedHashSet<>(Arrays.asList(supplier.get()));
		if (!StringUtils.hasLength(propertyValue)) {
			return !unset.equals(profiles);
		}
		Set<String> propertyProfiles = StringUtils
				.commaDelimitedListToSet(StringUtils.trimAllWhitespace(propertyValue));
		return !propertyProfiles.equals(profiles);
	}

	private List<String> expandAcceptedProfiles(List<String> activeProfiles, List<String> defaultProfiles) {
		Deque<String> stack = new ArrayDeque<>();
		asReversedList((!activeProfiles.isEmpty()) ? activeProfiles : defaultProfiles).forEach(stack::push);
		Set<String> acceptedProfiles = new LinkedHashSet<>();
		while (!stack.isEmpty()) {
			acceptedProfiles.add(stack.pop());
		}
		return asUniqueItemList(StringUtils.toStringArray(acceptedProfiles));
	}

	private List<String> asReversedList(List<String> list) {
		if (list == null || list.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> reversed = new ArrayList<>(list);
		Collections.reverse(reversed);
		return Collections.unmodifiableList(reversed);
	}

	private List<String> asUniqueItemList(String[] array) {
		return asUniqueItemList(array, null);
	}

	private List<String> asUniqueItemList(String[] array, Collection<String> additional) {
		LinkedHashSet<String> uniqueItems = new LinkedHashSet<>(Arrays.asList(array));
		if (!CollectionUtils.isEmpty(additional)) {
			uniqueItems.addAll(additional);
		}
		return Collections.unmodifiableList(new ArrayList<>(uniqueItems));
	}

	/**
	 * Return an iterator for all {@link #getAccepted() accepted profiles}.
	 */
	@Override
	public Iterator<String> iterator() {
		return getAccepted().iterator();
	}

	/**
	 * Return the active profiles.
	 * @return the active profiles
	 */
	public List<String> getActive() {
		return this.activeProfiles;
	}

	/**
	 * Return the default profiles.
	 * @return the active profiles
	 */
	public List<String> getDefault() {
		return this.defaultProfiles;
	}

	/**
	 * Return the accepted profiles.
	 * @return the accepted profiles
	 */
	public List<String> getAccepted() {
		return this.acceptedProfiles;
	}

	/**
	 * Return if the given profile is active.
	 * @param profile the profile to test
	 * @return if the profile is active
	 */
	public boolean isAccepted(String profile) {
		return this.acceptedProfiles.contains(profile);
	}

	@Override
	public String toString() {
		ToStringCreator creator = new ToStringCreator(this);
		creator.append("active", getActive().toString());
		creator.append("default", getDefault().toString());
		creator.append("accepted", getAccepted().toString());
		return creator.toString();
	}

}
