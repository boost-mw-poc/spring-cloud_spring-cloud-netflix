/*
 * Copyright 2017-present the original author or authors.
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

package org.springframework.cloud.netflix.eureka.http;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import com.netflix.discovery.shared.resolver.DefaultEndpoint;
import com.netflix.discovery.shared.transport.TransportClientFactory;
import org.junit.jupiter.api.Test;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientTransportClientFactoriesTests {

	@Test
	void shouldUseSslContextAndHostnameVerifierProvidedByDiscoveryClient() throws Exception {
		SSLContext sslContextFromArgs = SSLContext.getDefault();
		SSLContext sslContextFromDiscoveryClient = SSLContext.getInstance("TLS");
		sslContextFromDiscoveryClient.init(null, null, null);

		HostnameVerifier hostnameVerifierFromArgs = (hostname, session) -> false;
		HostnameVerifier hostnameVerifierFromDiscoveryClient = (hostname, session) -> true;

		AtomicReference<SSLContext> capturedSslContext = new AtomicReference<>();
		AtomicReference<HostnameVerifier> capturedHostnameVerifier = new AtomicReference<>();

		EurekaClientHttpRequestFactorySupplier requestFactorySupplier = (sslContext, hostnameVerifier) -> {
			capturedSslContext.set(sslContext);
			capturedHostnameVerifier.set(hostnameVerifier);
			return new SimpleClientHttpRequestFactory();
		};

		RestClientDiscoveryClientOptionalArgs args = new RestClientDiscoveryClientOptionalArgs(requestFactorySupplier,
				RestClient::builder);

		args.setSSLContext(sslContextFromArgs);
		args.setHostnameVerifier(hostnameVerifierFromArgs);

		RestClientTransportClientFactories factories = new RestClientTransportClientFactories(args);

		TransportClientFactory transportClientFactory = factories.newTransportClientFactory(null, null, null,
				Optional.of(sslContextFromDiscoveryClient), Optional.of(hostnameVerifierFromDiscoveryClient));

		transportClientFactory.newClient(new DefaultEndpoint("http://localhost"));

		assertThat(capturedSslContext.get()).isSameAs(sslContextFromDiscoveryClient);
		assertThat(capturedHostnameVerifier.get()).isSameAs(hostnameVerifierFromDiscoveryClient);
	}

}
