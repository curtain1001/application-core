package net.pingfang.core.metadata;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.alibaba.fastjson.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class SimplePropertyMetadata implements PropertyMetadata {

	private DataType valueType;

	private String id;

	private String name;

	private String description;

	private Map<String, Object> expands;

	public static SimplePropertyMetadata of(String id, String name, DataType type) {
		SimplePropertyMetadata metadata = new SimplePropertyMetadata();
		metadata.setId(id);
		metadata.setName(name);
		metadata.setValueType(type);
		return metadata;
	}

	@Override
	public void fromJson(JSONObject json) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PropertyMetadata merge(PropertyMetadata another, MergeOption... option) {
		SimplePropertyMetadata metadata = new SimplePropertyMetadata();
		metadata.setValueType(valueType);
		metadata.setId(id);
		metadata.setName(name);
		metadata.setDescription(description);
		metadata.setExpands(expands);
		if (metadata.expands == null) {
			metadata.expands = new HashMap<>();
		}
		if (MapUtils.isNotEmpty(another.getExpands())) {
			another.getExpands().forEach(metadata.expands::put);
		}
		return metadata;
	}
}
