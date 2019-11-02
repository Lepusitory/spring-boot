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

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.boot.cloudnativebuildpack.json.MappedObject;

/**
 * Image configuration information.
 *
 * @author Phillip Webb
 * @since 2.3.0
 */
public class ImageConfig extends MappedObject {

	private Map<String, String> labels;

	private final Map<String, String> configEnv;

	@SuppressWarnings("unchecked")
	ImageConfig(JsonNode node) {
		super(node, MethodHandles.lookup());
		this.labels = valueAt("/Labels", Map.class);
		this.configEnv = parseConfigEnv();
	}

	private Map<String, String> parseConfigEnv() {
		Map<String, String> env = new LinkedHashMap<>();
		String[] entries = valueAt("/Env", String[].class);
		for (String entry : entries) {
			int i = entry.indexOf('=');
			String name = (i != -1) ? entry.substring(0, i) : entry;
			String value = (i != -1) ? entry.substring(i + 1) : null;
			env.put(name, value);
		}
		return Collections.unmodifiableMap(env);
	}

	JsonNode getNodeCopy() {
		return super.getNode().deepCopy();
	}

	/**
	 * Return the image labels.
	 * @return the image labels
	 */
	public Map<String, String> getLabels() {
		return this.labels;
	}

	/**
	 * Return the image environment variables.
	 * @return the env
	 */
	public Map<String, String> getEnv() {
		return this.configEnv;
	}

	/**
	 * Create an updated copy of this image config.
	 * @param update consumer to apply updates
	 * @return an updated image config
	 */
	public ImageConfig copy(Consumer<Update> update) {
		return new Update(this).run(update);

	}

	/**
	 * Update class used to change data when creating a copy.
	 */
	public static final class Update {

		private ObjectNode copy;

		private Update(ImageConfig source) {
			this.copy = source.getNode().deepCopy();
		}

		private ImageConfig run(Consumer<Update> update) {
			update.accept(this);
			return new ImageConfig(this.copy);
		}

		/**
		 * Update the image config with an additional label.
		 * @param label the label name
		 * @param value the label value
		 */
		public void withLabel(String label, String value) {
			JsonNode labels = this.copy.at("/Labels");
			if (labels.isMissingNode()) {
				labels = this.copy.putObject("Labels");
			}
			((ObjectNode) labels).put(label, value);
		}

	}

}
