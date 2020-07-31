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

package org.springframework.boot.web.servlet;

import javax.servlet.annotation.WebListener;

/**
 * Interface to be implemented by types that register {@link WebListener @WebListeners}.
 *
 * @author Andy Wilkinson
 * @since 2.4.0
 */
public interface WebListenerRegistrar {

	/**
	 * Register web listeners with the given registry.
	 * @param registry the web listener registry
	 */
	void register(WebListenerRegistry registry);

}
