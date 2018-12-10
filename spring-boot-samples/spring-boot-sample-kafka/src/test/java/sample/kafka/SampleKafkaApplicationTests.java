/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.kafka;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for demo application.
 *
 * @author hcxin
 * @author Gary Russell
 * @author Stephane Nicoll
 */

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka
public class SampleKafkaApplicationTests {

	@Test
	public void testVanillaExchange(@Autowired Consumer consumer) throws Exception {
		long end = System.currentTimeMillis() + 10000;
		List<SampleMessage> messages = consumer.getMessages();
		while (messages.size() != 1 && System.currentTimeMillis() < end) {
			Thread.sleep(250);
		}
		assertThat(messages).hasSize(1);
		assertThat(messages.get(0).getMessage()).isEqualTo("A simple test message");
	}

}
