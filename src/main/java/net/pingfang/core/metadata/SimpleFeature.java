package net.pingfang.core.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleFeature implements org.jetlinks.core.metadata.Feature {
	private String id;
	private String name;
}
