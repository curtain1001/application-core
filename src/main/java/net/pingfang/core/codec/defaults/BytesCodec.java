package net.pingfang.core.codec.defaults;

import javax.annotation.Nonnull;

import net.pingfang.core.Payload;
import net.pingfang.core.codec.Codec;

public class BytesCodec implements Codec<byte[]> {

	public static BytesCodec INSTANCE = new BytesCodec();

	private BytesCodec() {

	}

	@Override
	public Class<byte[]> forType() {
		return byte[].class;
	}

	@Override
	public byte[] decode(@Nonnull Payload payload) {
		return payload.getBytes(false);
	}

	@Override
	public Payload encode(byte[] body) {
		return Payload.of(body);
	}

}
