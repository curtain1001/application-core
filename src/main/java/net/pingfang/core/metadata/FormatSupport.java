package net.pingfang.core.metadata;

/**
 * 格式化支持接口
 *
 * @author wangchao
 * @since 1.0
 */
public interface FormatSupport {
	/**
	 * 对值进行格式化
	 *
	 * @param value 值
	 * @return 格式化后的值
	 */
	Object format(Object value);
}
