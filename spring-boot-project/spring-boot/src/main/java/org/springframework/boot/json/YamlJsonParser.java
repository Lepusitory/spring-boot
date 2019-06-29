/*
 * Copyright 2012-2018 the original author or authors.
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

package org.springframework.boot.json;

import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Thin wrapper to adapt Snake {@link Yaml} to {@link JsonParser}.
 *
 * @author Dave Syer
 * @author Jean de Klerk
 * @since 1.0.0
 * @see JsonParserFactory
 */
public class YamlJsonParser extends AbstractJsonParser {

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> parseMap(String json) {
		return parseMap(json, (trimmed) -> new Yaml().loadAs(trimmed, Map.class));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Object> parseList(String json) {
		return parseList(json, (trimmed) -> new Yaml().loadAs(trimmed, List.class));
	}

}
