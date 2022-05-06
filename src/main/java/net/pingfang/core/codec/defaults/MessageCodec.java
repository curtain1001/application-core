package net.pingfang.core.codec.defaults;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.pingfang.core.Payload;
import net.pingfang.core.codec.Codec;
import net.pingfang.core.message.Message;
import net.pingfang.core.message.MessageType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageCodec implements Codec<Message> {
	public static MessageCodec INSTANCE = new MessageCodec();

	@Override
	public Class<Message> forType() {
		return Message.class;
	}

	@Nullable
	@Override
	public Message decode(@Nonnull Payload payload) {
		JSONObject json = JSON.parseObject(payload.bodyToString(false));
		return MessageType.convertMessage(json)
				.orElseThrow(() -> new UnsupportedOperationException("unsupported message : " + json));
	}

	@Override
	public Payload encode(Message body) {
		return Payload.of(JSON.toJSONBytes(body.toJson()));
	}
}
