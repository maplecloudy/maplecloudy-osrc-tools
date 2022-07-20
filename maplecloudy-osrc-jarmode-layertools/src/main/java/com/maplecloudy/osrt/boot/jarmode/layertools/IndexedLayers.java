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

package com.maplecloudy.osrt.boot.jarmode.layertools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * {@link Layers} implementation backed by a {@code layers.idx} file.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class IndexedLayers implements Layers {

	private final Map<String, List<String>> layers = new LinkedHashMap<>();

	IndexedLayers(String indexFile) {
		String[] lines = Arrays.stream(indexFile.split("\n")).map((line) -> line.replace("\r", ""))
				.filter(StringUtils::hasText).toArray(String[]::new);
		List<String> contents = null;
		for (String line : lines) {
			if (line.startsWith("- ")) {
				contents = new ArrayList<>();
				this.layers.put(line.substring(3, line.length() - 2), contents);
			}
			else if (line.startsWith("  - ")) {
				contents.add(line.substring(5, line.length() - 1));
			}
			else {
				throw new IllegalStateException("Layer index file is malformed");
			}
		}
		Assert.state(!this.layers.isEmpty(), "Empty layer index file loaded");
	}

	@Override
	public Iterator<String> iterator() {
		return this.layers.keySet().iterator();
	}

	@Override
	public String getLayer(ZipEntry entry) {
		return getLayer(entry.getName());
	}

	private String getLayer(String name) {
		for (Map.Entry<String, List<String>> entry : this.layers.entrySet()) {
			for (String candidate : entry.getValue()) {
				if (candidate.equals(name) || (candidate.endsWith("/") && name.startsWith(candidate))) {
					return entry.getKey();
				}
			}
		}
		throw new IllegalStateException("No layer defined in index for file '" + name + "'");
	}

	/**
	 * Get an {@link IndexedLayers} instance of possible.
	 * @param context the context
	 * @return an {@link IndexedLayers} instance or {@code null} if this not a layered
	 * jar.
	 */
	static IndexedLayers get(Context context) {
		try {
			try (JarFile jarFile = new JarFile(context.getArchiveFile())) {
				Manifest manifest = jarFile.getManifest();
				String location = manifest.getMainAttributes().getValue("Spring-Boot-Layers-Index");
				ZipEntry entry = (location != null) ? jarFile.getEntry(location) : null;
				if (entry != null) {
					String indexFile = StreamUtils.copyToString(jarFile.getInputStream(entry), StandardCharsets.UTF_8);
					return new IndexedLayers(indexFile);
				}
			}
			return null;
		}
		catch (FileNotFoundException | NoSuchFileException ex) {
			return null;
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
