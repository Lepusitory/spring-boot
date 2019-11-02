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

package org.springframework.boot.cloudnativebuildpack.docker.type;

import java.util.Random;
import java.util.stream.IntStream;

import org.springframework.util.Assert;

/**
 * Utility class used to generate random strings.
 *
 * @author Phillip Webb
 */
final class RandomString {

	private static final Random random = new Random();

	private RandomString() {
	}

	static String generate(String prefix, int randomLength) {
		Assert.notNull(prefix, "Prefix must not be null");
		return prefix + generateRandom(randomLength);
	}

	static CharSequence generateRandom(int length) {
		IntStream chars = random.ints('a', 'z' + 1).limit(length);
		return chars.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append);
	}

}
