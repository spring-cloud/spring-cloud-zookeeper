/*
 * Copyright 2015-2019 the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery;

/**
 * Utils for correct dependency path format.
 *
 * @author Denis Stepanov
 * @since 1.0.4
 */
public final class DependencyPathUtils {

	private DependencyPathUtils() {
	}

	/**
	 * Sanitizes path by ensuring that path starts with a slash and doesn't have one at
	 * the end.
	 * @param path file path to sanitize.
	 * @return sanitized path.
	 */
	public static String sanitize(String path) {
		return withLeadingSlash(withoutSlashAtEnd(path));
	}

	private static String withLeadingSlash(String value) {
		return value.startsWith("/") ? value : "/" + value;
	}

	private static String withoutSlashAtEnd(String value) {
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

}
