/*
 * Copyright 2012-2023 the original author or authors.
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

package org.springframework.boot.autoconfigure.amqp;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts {@link RabbitProperties} to {@link RabbitConnectionDetails}.
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
class PropertiesRabbitConnectionDetails implements RabbitConnectionDetails {

	private final RabbitProperties properties;

	PropertiesRabbitConnectionDetails(RabbitProperties properties) {
		this.properties = properties;
	}

	@Override
	public String getUsername() {
		return this.properties.determineUsername();
	}

	@Override
	public String getPassword() {
		return this.properties.determinePassword();
	}

	@Override
	public String getVirtualHost() {
		return this.properties.determineVirtualHost();
	}

	@Override
	public List<Address> getAddresses() {
		List<Address> addresses = new ArrayList<>();
		for (String address : this.properties.determineAddresses().split(",")) {
			int portSeparatorPosition = address.lastIndexOf(':');
			String host = address.substring(0, portSeparatorPosition);
			String port = address.substring(portSeparatorPosition + 1);
			addresses.add(new Address(host, Integer.parseInt(port)));
		}
		return addresses;
	}

}
