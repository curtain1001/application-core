package net.pingfang.core.metadata.unit;

import java.io.Serializable;
import java.util.Map;

import net.pingfang.core.metadata.FormatSupport;
import net.pingfang.core.metadata.Metadata;

/**
 * 值单位
 *
 * @author bsetfeng
 * @author zhouhao
 * @version 1.0
 **/
public interface ValueUnit extends Metadata, FormatSupport, Serializable {

	/**
	 * 单位符号
	 *
	 * @return 符号
	 */
	String getSymbol();

	@Override
	default Map<String, Object> getExpands() {
		return null;
	}
}
