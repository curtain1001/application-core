package net.pingfang.core.metadata.types;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import lombok.Getter;
import lombok.Setter;
import net.pingfang.core.config.ConfigKey;
import net.pingfang.core.config.ConfigKeyValue;
import net.pingfang.core.metadata.DataType;

@Getter
@Setter
@SuppressWarnings("all")
public abstract class AbstractType<R> implements DataType {

	private Map<String, Object> expands;

	private String description;

	public R expands(Map<String, Object> expands) {
		if (MapUtils.isEmpty(expands)) {
			return (R) this;
		}
		if (this.expands == null) {
			this.expands = new HashMap<>();
		}
		this.expands.putAll(expands);
		return (R) this;
	}

	public R expand(ConfigKeyValue<?>... kvs) {
		for (ConfigKeyValue<?> kv : kvs) {
			expand(kv.getKey(), kv.getValue());
		}
		return (R) this;
	}

	public <V> R expand(ConfigKey<V> configKey, V value) {
		return expand(configKey.getKey(), value);
	}

	public R expand(String configKey, Object value) {

		if (value == null) {
			return (R) this;
		}
		if (expands == null) {
			expands = new HashMap<>();
		}
		expands.put(configKey, value);
		return (R) this;
	}

	public R description(String description) {
		this.description = description;
		return (R) this;
	}

}
