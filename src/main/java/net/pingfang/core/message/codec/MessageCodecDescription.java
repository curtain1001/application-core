package net.pingfang.core.message.codec;

import javax.annotation.Nullable;

import org.jetlinks.core.metadata.ConfigMetadata;

/**
 * @see MqttMessageCodecDescription
 */
public interface MessageCodecDescription {

	String getDescription();

	@Nullable
	ConfigMetadata getConfigMetadata();

}
