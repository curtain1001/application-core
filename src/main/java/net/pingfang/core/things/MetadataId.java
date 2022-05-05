package net.pingfang.core.things;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class MetadataId {

	@NonNull
	private ThingMetadataType type;

	@NonNull
	private String id;

	@Override
	public String toString() {
		return type.name() + ":" + id;
	}
}
