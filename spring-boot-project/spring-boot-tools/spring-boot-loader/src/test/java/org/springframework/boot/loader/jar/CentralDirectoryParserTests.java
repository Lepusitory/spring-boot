/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.loader.jar;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.boot.loader.TestJarCreator;
import org.springframework.boot.loader.data.RandomAccessData;
import org.springframework.boot.loader.data.RandomAccessDataFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CentralDirectoryParser}.
 *
 * @author Phillip Webb
 */
public class CentralDirectoryParserTests {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File jarFile;

	private RandomAccessData jarData;

	@Before
	public void setup() throws Exception {
		this.jarFile = this.temporaryFolder.newFile();
		TestJarCreator.createTestJar(this.jarFile);
		this.jarData = new RandomAccessDataFile(this.jarFile);
	}

	@Test
	public void visitsInOrder() throws Exception {
		MockCentralDirectoryVisitor visitor = new MockCentralDirectoryVisitor();
		CentralDirectoryParser parser = new CentralDirectoryParser();
		parser.addVisitor(visitor);
		parser.parse(this.jarData, false);
		List<String> invocations = visitor.getInvocations();
		assertThat(invocations).startsWith("visitStart").endsWith("visitEnd").contains("visitFileHeader");
	}

	@Test
	public void visitRecords() throws Exception {
		Collector collector = new Collector();
		CentralDirectoryParser parser = new CentralDirectoryParser();
		parser.addVisitor(collector);
		parser.parse(this.jarData, false);
		Iterator<CentralDirectoryFileHeader> headers = collector.getHeaders().iterator();
		assertThat(headers.next().getName().toString()).isEqualTo("META-INF/");
		assertThat(headers.next().getName().toString()).isEqualTo("META-INF/MANIFEST.MF");
		assertThat(headers.next().getName().toString()).isEqualTo("1.dat");
		assertThat(headers.next().getName().toString()).isEqualTo("2.dat");
		assertThat(headers.next().getName().toString()).isEqualTo("d/");
		assertThat(headers.next().getName().toString()).isEqualTo("d/9.dat");
		assertThat(headers.next().getName().toString()).isEqualTo("special/");
		assertThat(headers.next().getName().toString()).isEqualTo("special/\u00EB.dat");
		assertThat(headers.next().getName().toString()).isEqualTo("nested.jar");
		assertThat(headers.next().getName().toString()).isEqualTo("another-nested.jar");
		assertThat(headers.next().getName().toString()).isEqualTo("space nested.jar");
		assertThat(headers.next().getName().toString()).isEqualTo("multi-release.jar");
		assertThat(headers.hasNext()).isFalse();
	}

	private static class Collector implements CentralDirectoryVisitor {

		private List<CentralDirectoryFileHeader> headers = new ArrayList<>();

		@Override
		public void visitStart(CentralDirectoryEndRecord endRecord, RandomAccessData centralDirectoryData) {
		}

		@Override
		public void visitFileHeader(CentralDirectoryFileHeader fileHeader, int dataOffset) {
			this.headers.add(fileHeader.clone());
		}

		@Override
		public void visitEnd() {
		}

		public List<CentralDirectoryFileHeader> getHeaders() {
			return this.headers;
		}

	}

	private static class MockCentralDirectoryVisitor implements CentralDirectoryVisitor {

		private final List<String> invocations = new ArrayList<>();

		@Override
		public void visitStart(CentralDirectoryEndRecord endRecord, RandomAccessData centralDirectoryData) {
			this.invocations.add("visitStart");
		}

		@Override
		public void visitFileHeader(CentralDirectoryFileHeader fileHeader, int dataOffset) {
			this.invocations.add("visitFileHeader");
		}

		@Override
		public void visitEnd() {
			this.invocations.add("visitEnd");
		}

		public List<String> getInvocations() {
			return this.invocations;
		}

	}

}
