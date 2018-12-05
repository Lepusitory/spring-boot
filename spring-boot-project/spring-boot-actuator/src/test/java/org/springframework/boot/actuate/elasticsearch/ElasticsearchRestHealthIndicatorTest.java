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

package org.springframework.boot.actuate.elasticsearch;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.when;

/**
 * Tests for {@link ElasticsearchRestHealthIndicator}.
 *
 * @author Artsiom Yudovin
 */
public class ElasticsearchRestHealthIndicatorTest {

	private final RestClient restClient = mock(RestClient.class);

	private final ElasticsearchRestHealthIndicator elasticsearchRestHealthIndicator = new ElasticsearchRestHealthIndicator(
			this.restClient);

	@Test
	public void elasticsearchIsUp() throws IOException {
		BasicHttpEntity httpEntity = new BasicHttpEntity();
		httpEntity.setContent(
				new ByteArrayInputStream(createJsonResult(200, "green").getBytes()));

		Response response = mock(Response.class);
		StatusLine statusLine = mock(StatusLine.class);

		when(statusLine.getStatusCode()).thenReturn(200);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(httpEntity);
		when(this.restClient.performRequest(any(Request.class))).thenReturn(response);

		assertThat(this.elasticsearchRestHealthIndicator.health().getStatus())
				.isEqualTo(Status.UP);
	}

	@Test
	public void elasticsearchIsDown() throws IOException {
		when(this.restClient.performRequest(any(Request.class)))
				.thenThrow(new IOException("Couldn't connect"));

		assertThat(this.elasticsearchRestHealthIndicator.health().getStatus())
				.isEqualTo(Status.DOWN);
	}

	@Test
	public void elasticsearchIsDownByResponseCode() throws IOException {

		Response response = mock(Response.class);
		StatusLine statusLine = mock(StatusLine.class);

		when(statusLine.getStatusCode()).thenReturn(500);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(this.restClient.performRequest(any(Request.class))).thenReturn(response);

		assertThat(this.elasticsearchRestHealthIndicator.health().getStatus())
				.isEqualTo(Status.DOWN);
	}

	@Test
	public void elasticsearchIsOutOfServiceByStatus() throws IOException {
		BasicHttpEntity httpEntity = new BasicHttpEntity();
		httpEntity.setContent(
				new ByteArrayInputStream(createJsonResult(200, "red").getBytes()));

		Response response = mock(Response.class);
		StatusLine statusLine = mock(StatusLine.class);

		when(statusLine.getStatusCode()).thenReturn(200);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(httpEntity);
		when(this.restClient.performRequest(any(Request.class))).thenReturn(response);

		assertThat(this.elasticsearchRestHealthIndicator.health().getStatus())
				.isEqualTo(Status.OUT_OF_SERVICE);
	}

	private String createJsonResult(int responseCode, String status) {
		String json;
		if (responseCode == 200) {
			json = String.format("{\"cluster_name\":\"elasticsearch\","
					+ "\"status\":\"%s\",\"timed_out\":false,\"number_of_nodes\":1,"
					+ "\"number_of_data_nodes\":1,\"active_primary_shards\":0,"
					+ "\"active_shards\":0,\"relocating_shards\":0,\"initializing_shards\":0,"
					+ "\"unassigned_shards\":0,\"delayed_unassigned_shards\":0,"
					+ "\"number_of_pending_tasks\":0,\"number_of_in_flight_fetch\":0,"
					+ "\"task_max_waiting_in_queue_millis\":0,\"active_shards_percent_as_number\":100.0}",
					status);
		}
		else {
			json = "{\n" + "  \"error\": \"Server Error\",\n" + "  \"status\": "
					+ responseCode + "\n" + "}";
		}

		return json;
	}

}
