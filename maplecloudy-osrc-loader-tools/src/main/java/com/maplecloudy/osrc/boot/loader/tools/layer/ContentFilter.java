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

package com.maplecloudy.osrc.boot.loader.tools.layer;

/**
 * Callback interface that can be used to filter layer contents.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 * @param <T> the content type
 * @since 2.3.0
 */
@FunctionalInterface
public interface ContentFilter<T> {

	/**
	 * Return if the filter matches the specified item.
	 * @param item the item to test
	 * @return if the filter matches
	 */
	boolean matches(T item);

}
