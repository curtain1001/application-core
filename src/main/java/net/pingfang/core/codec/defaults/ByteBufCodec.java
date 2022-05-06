package net.pingfang.core.codec.defaults;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import net.pingfang.core.Payload;
import net.pingfang.core.codec.Codec;

public class ByteBufCodec implements Codec<ByteBuf> {

	public static final ByteBufCodec INSTANCE = new ByteBufCodec();

	@Override
	public Class<ByteBuf> forType() {
		return ByteBuf.class;
	}

	@Override
	public ByteBuf decode(@Nonnull Payload payload) {
		return payload.getBody();
	}

	@Override
	public Payload encode(ByteBuf body) {
		return Payload.of(body);
	}
}
