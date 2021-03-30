/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.boot.jta.bitronix;

import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;

import org.springframework.boot.jms.XAConnectionFactoryWrapper;

/**
 * {@link XAConnectionFactoryWrapper} that uses a Bitronix
 * {@link PoolingConnectionFactoryBean} to wrap a {@link XAConnectionFactory}.
 *
 * @author Phillip Webb
 * @since 1.2.0
 * @deprecated since 2.3.0 for removal in 2.5.0 as the Bitronix project is no longer being
 * maintained
 */
@Deprecated
public class BitronixXAConnectionFactoryWrapper implements XAConnectionFactoryWrapper {

	@Override
	public ConnectionFactory wrapConnectionFactory(XAConnectionFactory connectionFactory) {
		PoolingConnectionFactoryBean pool = new PoolingConnectionFactoryBean();
		pool.setConnectionFactory(connectionFactory);
		return pool;
	}

}
