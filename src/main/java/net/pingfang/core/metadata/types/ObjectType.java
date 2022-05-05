package net.pingfang.core.metadata.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.core.metadata.*;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class ObjectType extends AbstractType<ObjectType>
		implements DataType, org.jetlinks.core.metadata.Converter<Map<String, Object>> {
	public static final String ID = "object";

	private List<org.jetlinks.core.metadata.PropertyMetadata> properties;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "对象类型";
	}

	public ObjectType addPropertyMetadata(org.jetlinks.core.metadata.PropertyMetadata property) {

		if (this.properties == null) {
			this.properties = new ArrayList<>();
		}

		this.properties.add(property);

		return this;
	}

	public List<org.jetlinks.core.metadata.PropertyMetadata> getProperties() {
		if (properties == null) {
			return Collections.emptyList();
		}
		return properties;
	}

	public ObjectType addProperty(String property, DataType type) {
		return this.addProperty(property, property, type);
	}

	public ObjectType addProperty(String property, String name, DataType type) {
		SimplePropertyMetadata metadata = new SimplePropertyMetadata();
		metadata.setId(property);
		metadata.setName(name);
		metadata.setValueType(type);
		return addPropertyMetadata(metadata);
	}

	@Override
	public ValidateResult validate(Object value) {

		if (properties == null || properties.isEmpty()) {
			return ValidateResult.success(value);
		}
		Map<String, Object> mapValue = convert(value);

		for (org.jetlinks.core.metadata.PropertyMetadata property : properties) {
			Object data = mapValue.get(property.getId());
			if (data == null) {
				continue;
			}
			ValidateResult result = property.getValueType().validate(data);
			if (!result.isSuccess()) {
				return result;
			}
		}
		return ValidateResult.success(mapValue);
	}

	@Override
	public JSONObject format(Object value) {
		return new JSONObject(handle(value, DataType::format));
	}

	@SuppressWarnings("all")
	public Map<String, Object> handle(Object value, BiFunction<DataType, Object, Object> mapping) {
		if (value == null) {
			return null;
		}
		if (value instanceof String && ((String) value).startsWith("{")) {
			value = JSON.parseObject(String.valueOf(value));
		}
		if (!(value instanceof Map)) {
			value = FastBeanCopier.copy(value, new HashMap<>());
		}
		if (value instanceof Map) {
			Map<String, Object> mapValue = new HashMap<>(((Map) value));
			if (properties != null) {
				for (org.jetlinks.core.metadata.PropertyMetadata property : properties) {
					Object data = mapValue.get(property.getId());
					DataType type = property.getValueType();
					if (data != null) {
						mapValue.put(property.getId(), mapping.apply(type, data));
					}
				}
			}
			return mapValue;
		}
		return null;
	}

	@Override
	public Map<String, Object> convert(Object value) {
		return handle(value, (type, data) -> {
			if (type instanceof org.jetlinks.core.metadata.Converter) {
				return ((org.jetlinks.core.metadata.Converter<?>) type).convert(data);
			}
			return data;
		});
	}

	public Optional<org.jetlinks.core.metadata.PropertyMetadata> getProperty(String key) {
		if (CollectionUtils.isEmpty(properties)) {
			return Optional.empty();
		}
		return properties.stream().filter(prop -> prop.getId().equals(key)).findAny();
	}
}
