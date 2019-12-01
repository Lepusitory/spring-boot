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

package org.springframework.boot.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link SslUtils}.
 *
 * @author Chris Bono
 */

public class SslUtilsTest {

	private static final String VALID_ALIAS = "test-alias";

	private static final String INVALID_ALIAS = "test-alias-5150";

	private KeyStore keyStore;

	@Before
	public void loadKeystore() throws Exception {
		this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		this.keyStore.load(new FileInputStream(new File("src/test/resources/test.jks")), "secret".toCharArray());
	}

	@Test
	public void assertStoreContainsAliasPassesWhenAliasFound() throws KeyStoreException {
		SslUtils.assertStoreContainsAlias(this.keyStore, VALID_ALIAS);
	}

	@Test
	public void assertStoreContainsAliasPassesWhenNullAlias() throws KeyStoreException {
		SslUtils.assertStoreContainsAlias(this.keyStore, null);
	}

	@Test
	public void assertStoreContainsAliasPassesWhenEmptyAlias() throws KeyStoreException {
		SslUtils.assertStoreContainsAlias(this.keyStore, "");
	}

	@Test
	public void assertStoreContainsAliasFailsWhenAliasNotFound() throws KeyStoreException {
		assertThatThrownBy(() -> SslUtils.assertStoreContainsAlias(this.keyStore, INVALID_ALIAS))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Keystore does not contain specified alias '" + INVALID_ALIAS + "'");
	}

	@Test
	public void assertStoreContainsAliasFailsWhenKeyStoreThrowsExceptionOnContains() throws KeyStoreException {
		KeyStore uninitializedKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		assertThatThrownBy(() -> SslUtils.assertStoreContainsAlias(uninitializedKeyStore, "alias"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Could not determine if keystore contains alias 'alias'");
	}

}
