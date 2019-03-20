/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.springframework.util.StringUtils;

/**
 * Quiet task that establishes a reference to its loader.
 *
 * @author Matt Benson
 * @since 1.3.0
 */
public class ShareAntlibLoader extends Task {

	private String refid;

	public ShareAntlibLoader(Project project) {
		setProject(project);
	}

	@Override
	public void execute() throws BuildException {
		if (!StringUtils.hasText(this.refid)) {
			throw new BuildException("@refid has no text");
		}
		getProject().addReference(this.refid, getClass().getClassLoader());
	}

	public void setRefid(String refid) {
		this.refid = refid;
	}

}
