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

package org.springframework.boot.autoconfigure.security.saml2;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.AssertingParty;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.AssertingParty.Verification;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.Decryption;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.Registration;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.Registration.Signing;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.core.Saml2X509Credential.Saml2X509CredentialType;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration.AssertingPartyDetails;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration.Builder;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link Configuration @Configuration} used to map {@link Saml2RelyingPartyProperties} to
 * relying party registrations in a {@link RelyingPartyRegistrationRepository}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@Configuration(proxyBeanMethods = false)
@Conditional(RegistrationConfiguredCondition.class)
@ConditionalOnMissingBean(RelyingPartyRegistrationRepository.class)
class Saml2RelyingPartyRegistrationConfiguration {

	private static final Log logger = LogFactory.getLog(Saml2RelyingPartyRegistrationConfiguration.class);

	@Bean
	RelyingPartyRegistrationRepository relyingPartyRegistrationRepository(Saml2RelyingPartyProperties properties) {
		List<RelyingPartyRegistration> registrations = properties.getRegistration().entrySet().stream()
				.map(this::asRegistration).collect(Collectors.toList());
		return new InMemoryRelyingPartyRegistrationRepository(registrations);
	}

	private RelyingPartyRegistration asRegistration(Map.Entry<String, Registration> entry) {
		return asRegistration(entry.getKey(), entry.getValue());
	}

	private RelyingPartyRegistration asRegistration(String id, Registration properties) {
		boolean usingMetadata = StringUtils
				.hasText(getFromAssertingParty(properties, id, "metadata-uri", AssertingParty::getMetadataUri));
		Builder builder = (usingMetadata) ? RelyingPartyRegistrations
				.fromMetadataLocation(
						getFromAssertingParty(properties, id, "metadata-uri", AssertingParty::getMetadataUri))
				.registrationId(id) : RelyingPartyRegistration.withRegistrationId(id);
		builder.assertionConsumerServiceLocation(properties.getAcs().getLocation());
		builder.assertionConsumerServiceBinding(properties.getAcs().getBinding());
		builder.assertingPartyDetails(mapAssertingParty(properties, id, usingMetadata));
		builder.signingX509Credentials((credentials) -> properties.getSigning().getCredentials().stream()
				.map(this::asSigningCredential).forEach(credentials::add));
		builder.decryptionX509Credentials((credentials) -> properties.getDecryption().getCredentials().stream()
				.map(this::asDecryptionCredential).forEach(credentials::add));
		builder.assertingPartyDetails((details) -> details.verificationX509Credentials(
				(credentials) -> getFromAssertingParty(properties, id, "verification", AssertingParty::getVerification)
						.getCredentials().stream().map(this::asVerificationCredential).forEach(credentials::add)));
		builder.entityId(properties.getEntityId());
		RelyingPartyRegistration registration = builder.build();
		boolean signRequest = registration.getAssertingPartyDetails().getWantAuthnRequestsSigned();
		validateSigningCredentials(properties, signRequest);
		return registration;
	}

	@SuppressWarnings("deprecation")
	private <T> T getFromAssertingParty(Registration registration, String id, String name,
			Function<AssertingParty, T> getter) {
		T newValue = getter.apply(registration.getAssertingParty());
		if (newValue != null) {
			return newValue;
		}
		T deprecatedValue = getter.apply(registration.getIdentityprovider());
		if (deprecatedValue != null) {
			logger.warn(String.format(
					"Property 'spring.security.saml2.relyingparty.registration.identityprovider.%1$s.%2$s' is deprecated, please use 'spring.security.saml2.relyingparty.registration.asserting-party.%1$s.%2$s' instead",
					id, name));
			return deprecatedValue;
		}
		return newValue;
	}

	private Consumer<AssertingPartyDetails.Builder> mapAssertingParty(Registration registration, String id,
			boolean usingMetadata) {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		return (details) -> {
			map.from(() -> getFromAssertingParty(registration, id, "entity-id", AssertingParty::getEntityId))
					.to(details::entityId);
			map.from(() -> getFromAssertingParty(registration, id, "singlesignon.binding",
					(property) -> property.getSinglesignon().getBinding())).to(details::singleSignOnServiceBinding);
			map.from(() -> getFromAssertingParty(registration, id, "singlesignon.url",
					(property) -> property.getSinglesignon().getUrl())).to(details::singleSignOnServiceLocation);
			map.from(() -> getFromAssertingParty(registration, id, "singlesignon.sign-request",
					(property) -> property.getSinglesignon().getSignRequest())).when((ignored) -> !usingMetadata)
					.to(details::wantAuthnRequestsSigned);
		};
	}

	private void validateSigningCredentials(Registration properties, boolean signRequest) {
		if (signRequest) {
			Assert.state(!properties.getSigning().getCredentials().isEmpty(),
					"Signing credentials must not be empty when authentication requests require signing.");
		}
	}

	private Saml2X509Credential asSigningCredential(Signing.Credential properties) {
		RSAPrivateKey privateKey = readPrivateKey(properties.getPrivateKeyLocation());
		X509Certificate certificate = readCertificate(properties.getCertificateLocation());
		return new Saml2X509Credential(privateKey, certificate, Saml2X509CredentialType.SIGNING);
	}

	private Saml2X509Credential asDecryptionCredential(Decryption.Credential properties) {
		RSAPrivateKey privateKey = readPrivateKey(properties.getPrivateKeyLocation());
		X509Certificate certificate = readCertificate(properties.getCertificateLocation());
		return new Saml2X509Credential(privateKey, certificate, Saml2X509CredentialType.DECRYPTION);
	}

	private Saml2X509Credential asVerificationCredential(Verification.Credential properties) {
		X509Certificate certificate = readCertificate(properties.getCertificateLocation());
		return new Saml2X509Credential(certificate, Saml2X509Credential.Saml2X509CredentialType.ENCRYPTION,
				Saml2X509Credential.Saml2X509CredentialType.VERIFICATION);
	}

	private RSAPrivateKey readPrivateKey(Resource location) {
		Assert.state(location != null, "No private key location specified");
		Assert.state(location.exists(), () -> "Private key location '" + location + "' does not exist");
		try (InputStream inputStream = location.getInputStream()) {
			return RsaKeyConverters.pkcs8().convert(inputStream);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	private X509Certificate readCertificate(Resource location) {
		Assert.state(location != null, "No certificate location specified");
		Assert.state(location.exists(), () -> "Certificate  location '" + location + "' does not exist");
		try (InputStream inputStream = location.getInputStream()) {
			return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

}
