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

package org.springframework.boot.layertools;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link HelpCommand}.
 *
 * @author Phillip Webb
 */
class HelpCommandTests {

	private HelpCommand command;

	private TestPrintStream out;

	@BeforeEach
	void setup() {
		Context context = mock(Context.class);
		given(context.getJarFile()).willReturn(new File("test.jar"));
		this.command = new HelpCommand(context, LayerToolsJarMode.Runner.getCommands(context));
		this.out = new TestPrintStream(this);
	}

	@Test
	void runWhenHasNoParametersPrintsUsage() {
		this.command.run(this.out, Collections.emptyMap(), Collections.emptyList());
		assertThat(this.out).hasSameContentAsResource("help-output.txt");
	}

	@Test
	void runWhenHasNoCommandParameterPrintsUsage() {
		this.command.run(this.out, Collections.emptyMap(), Arrays.asList("extract"));
		System.out.println(this.out);
		assertThat(this.out).hasSameContentAsResource("help-extract-output.txt");

	}

}
