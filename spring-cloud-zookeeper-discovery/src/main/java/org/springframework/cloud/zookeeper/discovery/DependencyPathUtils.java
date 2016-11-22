package org.springframework.cloud.zookeeper.discovery;

/**
 * Utils for correct dependency path format
 *
 * @author Denis Stepanov
 * @since 1.0.4
 */
public class DependencyPathUtils {

	/**
	 * Sanitizes path by ensuring that path starts with a slash and doesn't have one at the end
	 * @param path
	 * @return
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
