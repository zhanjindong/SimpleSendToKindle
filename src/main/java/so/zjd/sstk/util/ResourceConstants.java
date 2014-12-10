/**
 *  Copyright (c) 2014 zhanjindong. All rights reserved.
 */
package so.zjd.sstk.util;

public enum ResourceConstants {
	/** 从项目和jar中读取资源的URL前缀 */
	CLASSPATH_ALL_URL_PREFIX("classpath*:"),

	/** 从项目中读取资源的URL前缀 */
	CLASSPATH_URL_PREFIX("classpath:"),

	/** 从文件系统中读取资源的URL前缀 */
	FILE_URL_PREFIX("file:"),

	/** 上层路径 */
	TOP_PATH(".."),

	/** 当前路径 */
	CURRENT_PATH("."),

	/** linux文件夹分隔符 */
	FOLDER_SEPARATOR("/"),

	/** windows文件分隔符 */
	WINDOWS_FOLDER_SEPARATOR("\\");

	private String value;

	private ResourceConstants(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
