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

package com.maplecloudy.osrc.boot.buildpack.platform.docker.transport;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import com.maplecloudy.osrc.boot.buildpack.platform.docker.configuration.DockerConfiguration;
import com.maplecloudy.osrc.boot.buildpack.platform.docker.configuration.DockerHost;
import com.maplecloudy.osrc.boot.buildpack.platform.io.IOConsumer;
import com.maplecloudy.osrc.boot.buildpack.platform.system.Environment;

/**
 * HTTP transport used for docker access.
 *
 * @author Phillip Webb
 * @author Scott Frederick
 * @since 2.3.0
 */
public interface HttpTransport {

	/**
	 * Perform a HTTP GET operation.
	 * @param uri the destination URI (excluding any host/port)
	 * @return the operation response
	 * @throws IOException on IO error
	 */
	Response get(URI uri) throws IOException;

	/**
	 * Perform a HTTP POST operation.
	 * @param uri the destination URI (excluding any host/port)
	 * @return the operation response
	 * @throws IOException on IO error
	 */
	Response post(URI uri) throws IOException;

	/**
	 * Perform a HTTP POST operation.
	 * @param uri the destination URI (excluding any host/port)
	 * @param registryAuth registry authentication credentials
	 * @return the operation response
	 * @throws IOException on IO error
	 */
	Response post(URI uri, String registryAuth) throws IOException;

	/**
	 * Perform a HTTP POST operation.
	 * @param uri the destination URI (excluding any host/port)
	 * @param contentType the content type to write
	 * @param writer a content writer
	 * @return the operation response
	 * @throws IOException on IO error
	 */
	Response post(URI uri, String contentType, IOConsumer<OutputStream> writer) throws IOException;

	/**
	 * Perform a HTTP PUT operation.
	 * @param uri the destination URI (excluding any host/port)
	 * @param contentType the content type to write
	 * @param writer a content writer
	 * @return the operation response
	 * @throws IOException on IO error
	 */
	Response put(URI uri, String contentType, IOConsumer<OutputStream> writer) throws IOException;

	/**
	 * Perform a HTTP DELETE operation.
	 * @param uri the destination URI (excluding any host/port)
	 * @return the operation response
	 * @throws IOException on IO error
	 */
	Response delete(URI uri) throws IOException;

	/**
	 * Create the most suitable {@link HttpTransport} based on the
	 * {@link Environment#SYSTEM system environment}.
	 * @return a {@link HttpTransport} instance
	 */
	static HttpTransport create() {
		return create(Environment.SYSTEM);
	}

	/**
	 * Create the most suitable {@link HttpTransport} based on the
	 * {@link Environment#SYSTEM system environment}.
	 * @param dockerHost the Docker engine host configuration
	 * @return a {@link HttpTransport} instance
	 */
	static HttpTransport create(DockerHost dockerHost) {
		return create(Environment.SYSTEM, dockerHost);
	}

	/**
	 * Create the most suitable {@link HttpTransport} based on the given
	 * {@link Environment}.
	 * @param environment the source environment
	 * @return a {@link HttpTransport} instance
	 */
	static HttpTransport create(Environment environment) {
		return create(environment, null);
	}

	/**
	 * Create the most suitable {@link HttpTransport} based on the given
	 * {@link Environment} and {@link DockerConfiguration}.
	 * @param environment the source environment
	 * @param dockerHost the Docker engine host configuration
	 * @return a {@link HttpTransport} instance
	 */
	static HttpTransport create(Environment environment, DockerHost dockerHost) {
		HttpTransport remote = RemoteHttpClientTransport.createIfPossible(environment, dockerHost);
		return (remote != null) ? remote : LocalHttpClientTransport.create(environment);
	}

	/**
	 * An HTTP operation response.
	 */
	interface Response extends Closeable {

		/**
		 * Return the content of the response.
		 * @return the response content
		 * @throws IOException on IO error
		 */
		InputStream getContent() throws IOException;

	}

}
