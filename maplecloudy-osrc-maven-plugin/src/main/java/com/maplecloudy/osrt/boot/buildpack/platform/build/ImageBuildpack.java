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

package com.maplecloudy.osrt.boot.buildpack.platform.build;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.util.StreamUtils;

import com.maplecloudy.osrt.boot.buildpack.platform.docker.transport.DockerEngineException;
import com.maplecloudy.osrt.boot.buildpack.platform.docker.type.Image;
import com.maplecloudy.osrt.boot.buildpack.platform.docker.type.ImageReference;
import com.maplecloudy.osrt.boot.buildpack.platform.docker.type.Layer;
import com.maplecloudy.osrt.boot.buildpack.platform.io.IOConsumer;
import com.maplecloudy.osrt.boot.buildpack.platform.io.TarArchive;

/**
 * A {@link Buildpack} that references a buildpack contained in an OCI image.
 *
 * The reference must be an OCI image reference. The reference can optionally contain a
 * prefix {@code docker://} to unambiguously identify it as an image buildpack reference.
 *
 * @author Scott Frederick
 * @author Phillip Webb
 */
final class ImageBuildpack implements Buildpack {

	private static final String PREFIX = "docker://";

	private final BuildpackCoordinates coordinates;

	private final ExportedLayers exportedLayers;

	private ImageBuildpack(BuildpackResolverContext context, ImageReference imageReference) {
		try {
			Image image = context.fetchImage(imageReference, ImageType.BUILDPACK);
			BuildpackMetadata buildpackMetadata = BuildpackMetadata.fromImage(image);
			this.coordinates = BuildpackCoordinates.fromBuildpackMetadata(buildpackMetadata);
			this.exportedLayers = new ExportedLayers(context, imageReference);
		}
		catch (IOException | DockerEngineException ex) {
			throw new IllegalArgumentException("Error pulling buildpack image '" + imageReference + "'", ex);
		}
	}

	@Override
	public BuildpackCoordinates getCoordinates() {
		return this.coordinates;
	}

	@Override
	public void apply(IOConsumer<Layer> layers) throws IOException {
		this.exportedLayers.apply(layers);
	}

	/**
	 * A {@link BuildpackResolver} compatible method to resolve image buildpacks.
	 * @param context the resolver context
	 * @param reference the buildpack reference
	 * @return the resolved {@link Buildpack} or {@code null}
	 */
	static Buildpack resolve(BuildpackResolverContext context, BuildpackReference reference) {
		boolean unambiguous = reference.hasPrefix(PREFIX);
		try {
			ImageReference imageReference = ImageReference
					.of((unambiguous) ? reference.getSubReference(PREFIX) : reference.toString());
			return new ImageBuildpack(context, imageReference);
		}
		catch (IllegalArgumentException ex) {
			if (unambiguous) {
				throw ex;
			}
			return null;
		}
	}

	private static class ExportedLayers {

		private final List<Path> layerFiles;

		ExportedLayers(BuildpackResolverContext context, ImageReference imageReference) throws IOException {
			List<Path> layerFiles = new ArrayList<>();
			context.exportImageLayers(imageReference, (name, archive) -> layerFiles.add(copyToTemp(name, archive)));
			this.layerFiles = Collections.unmodifiableList(layerFiles);
		}

		private Path copyToTemp(String name, TarArchive archive) throws IOException {
			String[] parts = name.split("/");
			Path path = Files.createTempFile("create-builder-scratch-", parts[0]);
			try (OutputStream out = Files.newOutputStream(path)) {
				archive.writeTo(out);
			}
			return path;
		}

		void apply(IOConsumer<Layer> layers) throws IOException {
			for (Path path : this.layerFiles) {
				layers.accept(Layer.fromTarArchive((out) -> copyLayerTar(path, out)));
			}
		}

		private void copyLayerTar(Path path, OutputStream out) throws IOException {
			try (TarArchiveInputStream tarIn = new TarArchiveInputStream(Files.newInputStream(path));
					TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
				tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
				TarArchiveEntry entry = tarIn.getNextTarEntry();
				while (entry != null) {
					tarOut.putArchiveEntry(entry);
					StreamUtils.copy(tarIn, tarOut);
					tarOut.closeArchiveEntry();
					entry = tarIn.getNextTarEntry();
				}
				tarOut.finish();
			}
			Files.delete(path);
		}

	}

}
