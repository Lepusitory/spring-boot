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

package org.springframework.boot.autoconfigure.web;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.undertow.UndertowOptions;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.coyote.AbstractProtocol;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.jupiter.api.Test;
import reactor.netty.http.HttpDecoderSpec;
import reactor.netty.http.server.HttpRequestDecoderSpec;

import org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.Accesslog;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ServerProperties}.
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Eddú Meléndez
 * @author Quinten De Swaef
 * @author Venil Noronha
 * @author Andrew McGhie
 * @author HaiTao Zhang
 * @author Rafiullah Hamedy
 * @author Chris Bono
 * @author Parviz Rozikov
 */
class ServerPropertiesTests {

	private final ServerProperties properties = new ServerProperties();

	@Test
	void testAddressBinding() throws Exception {
		bind("server.address", "127.0.0.1");
		assertThat(this.properties.getAddress()).isEqualTo(InetAddress.getByName("127.0.0.1"));
	}

	@Test
	void testPortBinding() {
		bind("server.port", "9000");
		assertThat(this.properties.getPort().intValue()).isEqualTo(9000);
	}

	@Test
	void testServerHeaderDefault() {
		assertThat(this.properties.getServerHeader()).isNull();
	}

	@Test
	void testServerHeader() {
		bind("server.server-header", "Custom Server");
		assertThat(this.properties.getServerHeader()).isEqualTo("Custom Server");
	}

	@Test
	void testTomcatBinding() {
		Map<String, String> map = new HashMap<>();
		map.put("server.tomcat.accesslog.conditionIf", "foo");
		map.put("server.tomcat.accesslog.conditionUnless", "bar");
		map.put("server.tomcat.accesslog.pattern", "%h %t '%r' %s %b");
		map.put("server.tomcat.accesslog.prefix", "foo");
		map.put("server.tomcat.accesslog.suffix", "-bar.log");
		map.put("server.tomcat.accesslog.encoding", "UTF-8");
		map.put("server.tomcat.accesslog.locale", "en-AU");
		map.put("server.tomcat.accesslog.checkExists", "true");
		map.put("server.tomcat.accesslog.rotate", "false");
		map.put("server.tomcat.accesslog.rename-on-rotate", "true");
		map.put("server.tomcat.accesslog.ipv6Canonical", "true");
		map.put("server.tomcat.accesslog.request-attributes-enabled", "true");
		map.put("server.tomcat.remoteip.protocol-header", "X-Forwarded-Protocol");
		map.put("server.tomcat.remoteip.remote-ip-header", "Remote-Ip");
		map.put("server.tomcat.remoteip.internal-proxies", "10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
		map.put("server.tomcat.remoteip.trusted-proxies", "proxy1|proxy2|proxy3");
		map.put("server.tomcat.reject-illegal-header", "false");
		map.put("server.tomcat.background-processor-delay", "10");
		map.put("server.tomcat.relaxed-path-chars", "|,<");
		map.put("server.tomcat.relaxed-query-chars", "^  ,  | ");
		map.put("server.tomcat.use-relative-redirects", "true");
		bind(map);
		ServerProperties.Tomcat tomcat = this.properties.getTomcat();
		Accesslog accesslog = tomcat.getAccesslog();
		assertThat(accesslog.getConditionIf()).isEqualTo("foo");
		assertThat(accesslog.getConditionUnless()).isEqualTo("bar");
		assertThat(accesslog.getPattern()).isEqualTo("%h %t '%r' %s %b");
		assertThat(accesslog.getPrefix()).isEqualTo("foo");
		assertThat(accesslog.getSuffix()).isEqualTo("-bar.log");
		assertThat(accesslog.getEncoding()).isEqualTo("UTF-8");
		assertThat(accesslog.getLocale()).isEqualTo("en-AU");
		assertThat(accesslog.isCheckExists()).isEqualTo(true);
		assertThat(accesslog.isRotate()).isFalse();
		assertThat(accesslog.isRenameOnRotate()).isTrue();
		assertThat(accesslog.isIpv6Canonical()).isTrue();
		assertThat(accesslog.isRequestAttributesEnabled()).isTrue();
		assertThat(tomcat.getRemoteip().getRemoteIpHeader()).isEqualTo("Remote-Ip");
		assertThat(tomcat.getRemoteip().getProtocolHeader()).isEqualTo("X-Forwarded-Protocol");
		assertThat(tomcat.getRemoteip().getInternalProxies()).isEqualTo("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
		assertThat(tomcat.getRemoteip().getTrustedProxies()).isEqualTo("proxy1|proxy2|proxy3");
		assertThat(tomcat.isRejectIllegalHeader()).isFalse();
		assertThat(tomcat.getBackgroundProcessorDelay()).hasSeconds(10);
		assertThat(tomcat.getRelaxedPathChars()).containsExactly('|', '<');
		assertThat(tomcat.getRelaxedQueryChars()).containsExactly('^', '|');
		assertThat(tomcat.isUseRelativeRedirects()).isTrue();
	}

	@Test
	void testTrailingSlashOfContextPathIsRemoved() {
		bind("server.servlet.context-path", "/foo/");
		assertThat(this.properties.getServlet().getContextPath()).isEqualTo("/foo");
	}

	@Test
	void testSlashOfContextPathIsDefaultValue() {
		bind("server.servlet.context-path", "/");
		assertThat(this.properties.getServlet().getContextPath()).isEqualTo("");
	}

	@Test
	void testContextPathWithLeadingWhitespace() {
		bind("server.servlet.context-path", " /assets");
		assertThat(this.properties.getServlet().getContextPath()).isEqualTo("/assets");
	}

	@Test
	void testContextPathWithTrailingWhitespace() {
		bind("server.servlet.context-path", "/assets/copy/ ");
		assertThat(this.properties.getServlet().getContextPath()).isEqualTo("/assets/copy");
	}

	@Test
	void testContextPathWithLeadingAndTrailingWhitespace() {
		bind("server.servlet.context-path", " /assets ");
		assertThat(this.properties.getServlet().getContextPath()).isEqualTo("/assets");
	}

	@Test
	void testContextPathWithLeadingAndTrailingWhitespaceAndContextWithSpace() {
		bind("server.servlet.context-path", "  /assets /copy/    ");
		assertThat(this.properties.getServlet().getContextPath()).isEqualTo("/assets /copy");
	}

	@Test
	void testCustomizeUriEncoding() {
		bind("server.tomcat.uri-encoding", "US-ASCII");
		assertThat(this.properties.getTomcat().getUriEncoding()).isEqualTo(StandardCharsets.US_ASCII);
	}

	@Test
	@SuppressWarnings("removal")
	@Deprecated(since = "3.0.0", forRemoval = true)
	void testCustomizeHeaderSize() {
		bind("server.max-http-header-size", "1MB");
		assertThat(this.properties.getMaxHttpHeaderSize()).isEqualTo(DataSize.ofMegabytes(1));
		assertThat(this.properties.getMaxHttpRequestHeaderSize()).isEqualTo(DataSize.ofMegabytes(1));
	}

	@Test
	@SuppressWarnings("removal")
	@Deprecated(since = "3.0.0", forRemoval = true)
	void testCustomizeHeaderSizeUseBytesByDefault() {
		bind("server.max-http-header-size", "1024");
		assertThat(this.properties.getMaxHttpHeaderSize()).isEqualTo(DataSize.ofKilobytes(1));
		assertThat(this.properties.getMaxHttpRequestHeaderSize()).isEqualTo(DataSize.ofKilobytes(1));
	}

	@Test
	void testCustomizeMaxHttpRequestHeaderSize() {
		bind("server.max-http-request-header-size", "1MB");
		assertThat(this.properties.getMaxHttpRequestHeaderSize()).isEqualTo(DataSize.ofMegabytes(1));
	}

	@Test
	void testCustomizeMaxHttpRequestHeaderSizeUseBytesByDefault() {
		bind("server.max-http-request-header-size", "1024");
		assertThat(this.properties.getMaxHttpRequestHeaderSize()).isEqualTo(DataSize.ofKilobytes(1));
	}

	@Test
	void testCustomizeTomcatMaxThreads() {
		bind("server.tomcat.threads.max", "10");
		assertThat(this.properties.getTomcat().getThreads().getMax()).isEqualTo(10);
	}

	@Test
	void testCustomizeTomcatKeepAliveTimeout() {
		bind("server.tomcat.keep-alive-timeout", "30s");
		assertThat(this.properties.getTomcat().getKeepAliveTimeout()).hasSeconds(30);
	}

	@Test
	void testCustomizeTomcatKeepAliveTimeoutWithInfinite() {
		bind("server.tomcat.keep-alive-timeout", "-1");
		assertThat(this.properties.getTomcat().getKeepAliveTimeout()).hasMillis(-1);
	}

	@Test
	void customizeMaxKeepAliveRequests() {
		bind("server.tomcat.max-keep-alive-requests", "200");
		assertThat(this.properties.getTomcat().getMaxKeepAliveRequests()).isEqualTo(200);
	}

	@Test
	void customizeMaxKeepAliveRequestsWithInfinite() {
		bind("server.tomcat.max-keep-alive-requests", "-1");
		assertThat(this.properties.getTomcat().getMaxKeepAliveRequests()).isEqualTo(-1);
	}

	@Test
	void testCustomizeTomcatMinSpareThreads() {
		bind("server.tomcat.threads.min-spare", "10");
		assertThat(this.properties.getTomcat().getThreads().getMinSpare()).isEqualTo(10);
	}

	@Test
	void testCustomizeJettyAcceptors() {
		bind("server.jetty.threads.acceptors", "10");
		assertThat(this.properties.getJetty().getThreads().getAcceptors()).isEqualTo(10);
	}

	@Test
	void testCustomizeJettySelectors() {
		bind("server.jetty.threads.selectors", "10");
		assertThat(this.properties.getJetty().getThreads().getSelectors()).isEqualTo(10);
	}

	@Test
	void testCustomizeJettyMaxThreads() {
		bind("server.jetty.threads.max", "10");
		assertThat(this.properties.getJetty().getThreads().getMax()).isEqualTo(10);
	}

	@Test
	void testCustomizeJettyMinThreads() {
		bind("server.jetty.threads.min", "10");
		assertThat(this.properties.getJetty().getThreads().getMin()).isEqualTo(10);
	}

	@Test
	void testCustomizeJettyIdleTimeout() {
		bind("server.jetty.threads.idle-timeout", "10s");
		assertThat(this.properties.getJetty().getThreads().getIdleTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	void testCustomizeJettyMaxQueueCapacity() {
		bind("server.jetty.threads.max-queue-capacity", "5150");
		assertThat(this.properties.getJetty().getThreads().getMaxQueueCapacity()).isEqualTo(5150);
	}

	@Test
	void testCustomizeUndertowServerOption() {
		bind("server.undertow.options.server.ALWAYS_SET_KEEP_ALIVE", "true");
		assertThat(this.properties.getUndertow().getOptions().getServer()).containsEntry("ALWAYS_SET_KEEP_ALIVE",
				"true");
	}

	@Test
	void testCustomizeUndertowSocketOption() {
		bind("server.undertow.options.socket.ALWAYS_SET_KEEP_ALIVE", "true");
		assertThat(this.properties.getUndertow().getOptions().getSocket()).containsEntry("ALWAYS_SET_KEEP_ALIVE",
				"true");
	}

	@Test
	void testCustomizeUndertowIoThreads() {
		bind("server.undertow.threads.io", "4");
		assertThat(this.properties.getUndertow().getThreads().getIo()).isEqualTo(4);
	}

	@Test
	void testCustomizeUndertowWorkerThreads() {
		bind("server.undertow.threads.worker", "10");
		assertThat(this.properties.getUndertow().getThreads().getWorker()).isEqualTo(10);
	}

	@Test
	void testCustomizeJettyAccessLog() {
		Map<String, String> map = new HashMap<>();
		map.put("server.jetty.accesslog.enabled", "true");
		map.put("server.jetty.accesslog.filename", "foo.txt");
		map.put("server.jetty.accesslog.file-date-format", "yyyymmdd");
		map.put("server.jetty.accesslog.retention-period", "4");
		map.put("server.jetty.accesslog.append", "true");
		map.put("server.jetty.accesslog.custom-format", "{client}a - %u %t \"%r\" %s %O");
		map.put("server.jetty.accesslog.ignore-paths", "/a/path,/b/path");
		bind(map);
		ServerProperties.Jetty jetty = this.properties.getJetty();
		assertThat(jetty.getAccesslog().isEnabled()).isTrue();
		assertThat(jetty.getAccesslog().getFilename()).isEqualTo("foo.txt");
		assertThat(jetty.getAccesslog().getFileDateFormat()).isEqualTo("yyyymmdd");
		assertThat(jetty.getAccesslog().getRetentionPeriod()).isEqualTo(4);
		assertThat(jetty.getAccesslog().isAppend()).isTrue();
		assertThat(jetty.getAccesslog().getCustomFormat()).isEqualTo("{client}a - %u %t \"%r\" %s %O");
		assertThat(jetty.getAccesslog().getIgnorePaths()).containsExactly("/a/path", "/b/path");
	}

	@Test
	void testCustomizeNettyIdleTimeout() {
		bind("server.netty.idle-timeout", "10s");
		assertThat(this.properties.getNetty().getIdleTimeout()).isEqualTo(Duration.ofSeconds(10));
	}

	@Test
	void testCustomizeNettyMaxKeepAliveRequests() {
		bind("server.netty.max-keep-alive-requests", "100");
		assertThat(this.properties.getNetty().getMaxKeepAliveRequests()).isEqualTo(100);
	}

	@Test
	void tomcatAcceptCountMatchesProtocolDefault() throws Exception {
		assertThat(this.properties.getTomcat().getAcceptCount()).isEqualTo(getDefaultProtocol().getAcceptCount());
	}

	@Test
	void tomcatProcessorCacheMatchesProtocolDefault() throws Exception {
		assertThat(this.properties.getTomcat().getProcessorCache()).isEqualTo(getDefaultProtocol().getProcessorCache());
	}

	@Test
	void tomcatMaxConnectionsMatchesProtocolDefault() throws Exception {
		assertThat(this.properties.getTomcat().getMaxConnections()).isEqualTo(getDefaultProtocol().getMaxConnections());
	}

	@Test
	void tomcatMaxThreadsMatchesProtocolDefault() throws Exception {
		assertThat(this.properties.getTomcat().getThreads().getMax()).isEqualTo(getDefaultProtocol().getMaxThreads());
	}

	@Test
	void tomcatMinSpareThreadsMatchesProtocolDefault() throws Exception {
		assertThat(this.properties.getTomcat().getThreads().getMinSpare())
				.isEqualTo(getDefaultProtocol().getMinSpareThreads());
	}

	@Test
	void tomcatMaxHttpPostSizeMatchesConnectorDefault() {
		assertThat(this.properties.getTomcat().getMaxHttpFormPostSize().toBytes())
				.isEqualTo(getDefaultConnector().getMaxPostSize());
	}

	@Test
	void tomcatBackgroundProcessorDelayMatchesEngineDefault() {
		assertThat(this.properties.getTomcat().getBackgroundProcessorDelay())
				.hasSeconds((new StandardEngine().getBackgroundProcessorDelay()));
	}

	@Test
	void tomcatMaxHttpFormPostSizeMatchesConnectorDefault() {
		assertThat(this.properties.getTomcat().getMaxHttpFormPostSize().toBytes())
				.isEqualTo(getDefaultConnector().getMaxPostSize());
	}

	@Test
	void tomcatUriEncodingMatchesConnectorDefault() {
		assertThat(this.properties.getTomcat().getUriEncoding().name())
				.isEqualTo(getDefaultConnector().getURIEncoding());
	}

	@Test
	void tomcatRedirectContextRootMatchesDefault() {
		assertThat(this.properties.getTomcat().getRedirectContextRoot())
				.isEqualTo(new StandardContext().getMapperContextRootRedirectEnabled());
	}

	@Test
	void tomcatAccessLogRenameOnRotateMatchesDefault() {
		assertThat(this.properties.getTomcat().getAccesslog().isRenameOnRotate())
				.isEqualTo(new AccessLogValve().isRenameOnRotate());
	}

	@Test
	void tomcatAccessLogRequestAttributesEnabledMatchesDefault() {
		assertThat(this.properties.getTomcat().getAccesslog().isRequestAttributesEnabled())
				.isEqualTo(new AccessLogValve().getRequestAttributesEnabled());
	}

	@Test
	void tomcatInternalProxiesMatchesDefault() {
		assertThat(this.properties.getTomcat().getRemoteip().getInternalProxies())
				.isEqualTo(new RemoteIpValve().getInternalProxies());
	}

	@Test
	void tomcatRejectIllegalHeaderMatchesProtocolDefault() throws Exception {
		assertThat(getDefaultProtocol()).hasFieldOrPropertyWithValue("rejectIllegalHeader",
				this.properties.getTomcat().isRejectIllegalHeader());
	}

	@Test
	void tomcatUseRelativeRedirectsDefaultsToFalse() {
		assertThat(this.properties.getTomcat().isUseRelativeRedirects()).isFalse();
	}

	@Test
	void tomcatMaxKeepAliveRequestsDefault() throws Exception {
		AbstractEndpoint<?, ?> endpoint = (AbstractEndpoint<?, ?>) ReflectionTestUtils.getField(getDefaultProtocol(),
				"endpoint");
		int defaultMaxKeepAliveRequests = (int) ReflectionTestUtils.getField(endpoint, "maxKeepAliveRequests");
		assertThat(this.properties.getTomcat().getMaxKeepAliveRequests()).isEqualTo(defaultMaxKeepAliveRequests);
	}

	@Test
	void jettyThreadPoolPropertyDefaultsShouldMatchServerDefault() {
		JettyServletWebServerFactory jettyFactory = new JettyServletWebServerFactory(0);
		JettyWebServer jetty = (JettyWebServer) jettyFactory.getWebServer();
		Server server = jetty.getServer();
		QueuedThreadPool threadPool = (QueuedThreadPool) server.getThreadPool();
		int idleTimeout = threadPool.getIdleTimeout();
		int maxThreads = threadPool.getMaxThreads();
		int minThreads = threadPool.getMinThreads();
		assertThat(this.properties.getJetty().getThreads().getIdleTimeout().toMillis()).isEqualTo(idleTimeout);
		assertThat(this.properties.getJetty().getThreads().getMax()).isEqualTo(maxThreads);
		assertThat(this.properties.getJetty().getThreads().getMin()).isEqualTo(minThreads);
	}

	@Test
	void jettyMaxHttpFormPostSizeMatchesDefault() {
		JettyServletWebServerFactory jettyFactory = new JettyServletWebServerFactory(0);
		JettyWebServer jetty = (JettyWebServer) jettyFactory
				.getWebServer((ServletContextInitializer) (servletContext) -> servletContext
						.addServlet("formPost", new HttpServlet() {

							@Override
							protected void doPost(HttpServletRequest req, HttpServletResponse resp)
									throws ServletException, IOException {
								req.getParameterMap();
							}

						}).addMapping("/form"));
		jetty.start();
		org.eclipse.jetty.server.Connector connector = jetty.getServer().getConnectors()[0];
		final AtomicReference<Throwable> failure = new AtomicReference<>();
		connector.addBean(new HttpChannel.Listener() {

			@Override
			public void onDispatchFailure(Request request, Throwable ex) {
				failure.set(ex);
			}

		});
		try {
			RestTemplate template = new RestTemplate();
			template.setErrorHandler(new ResponseErrorHandler() {

				@Override
				public boolean hasError(ClientHttpResponse response) throws IOException {
					return false;
				}

				@Override
				public void handleError(ClientHttpResponse response) throws IOException {

				}

			});
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("data", "a".repeat(250000));
			HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
			template.postForEntity(URI.create("http://localhost:" + jetty.getPort() + "/form"), entity, Void.class);
			assertThat(failure.get()).isNotNull();
			String message = failure.get().getCause().getMessage();
			int defaultMaxPostSize = Integer.parseInt(message.substring(message.lastIndexOf(' ')).trim());
			assertThat(this.properties.getJetty().getMaxHttpFormPostSize().toBytes()).isEqualTo(defaultMaxPostSize);
		}
		finally {
			jetty.stop();
		}
	}

	@Test
	void undertowMaxHttpPostSizeMatchesDefault() {
		assertThat(this.properties.getUndertow().getMaxHttpPostSize().toBytes())
				.isEqualTo(UndertowOptions.DEFAULT_MAX_ENTITY_SIZE);
	}

	@Test
	@Deprecated(since = "3.0.0", forRemoval = true)
	@SuppressWarnings("removal")
	void nettyMaxChunkSizeMatchesHttpDecoderSpecDefault() {
		assertThat(this.properties.getNetty().getMaxChunkSize().toBytes())
				.isEqualTo(HttpDecoderSpec.DEFAULT_MAX_CHUNK_SIZE);
	}

	@Test
	void nettyMaxInitialLineLengthMatchesHttpDecoderSpecDefault() {
		assertThat(this.properties.getNetty().getMaxInitialLineLength().toBytes())
				.isEqualTo(HttpDecoderSpec.DEFAULT_MAX_INITIAL_LINE_LENGTH);
	}

	@Test
	void nettyValidateHeadersMatchesHttpDecoderSpecDefault() {
		assertThat(this.properties.getNetty().isValidateHeaders()).isEqualTo(HttpDecoderSpec.DEFAULT_VALIDATE_HEADERS);
	}

	@Test
	void nettyH2cMaxContentLengthMatchesHttpDecoderSpecDefault() {
		assertThat(this.properties.getNetty().getH2cMaxContentLength().toBytes())
				.isEqualTo(HttpRequestDecoderSpec.DEFAULT_H2C_MAX_CONTENT_LENGTH);
	}

	@Test
	void nettyInitialBufferSizeMatchesHttpDecoderSpecDefault() {
		assertThat(this.properties.getNetty().getInitialBufferSize().toBytes())
				.isEqualTo(HttpDecoderSpec.DEFAULT_INITIAL_BUFFER_SIZE);
	}

	private Connector getDefaultConnector() {
		return new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
	}

	private AbstractProtocol<?> getDefaultProtocol() throws Exception {
		return (AbstractProtocol<?>) Class.forName(TomcatServletWebServerFactory.DEFAULT_PROTOCOL)
				.getDeclaredConstructor().newInstance();
	}

	private void bind(String name, String value) {
		bind(Collections.singletonMap(name, value));
	}

	private void bind(Map<String, String> map) {
		ConfigurationPropertySource source = new MapConfigurationPropertySource(map);
		new Binder(source).bind("server", Bindable.ofInstance(this.properties));
	}

}
