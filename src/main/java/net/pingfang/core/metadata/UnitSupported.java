package net.pingfang.core.metadata;

import net.pingfang.core.metadata.unit.ValueUnit;

/**
 * 单位支持
 */
public interface UnitSupported {
	ValueUnit getUnit();

	void setUnit(ValueUnit unit);
}
