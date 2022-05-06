package net.pingfang.core.codec.defaults;

import javax.annotation.Nonnull;

import net.pingfang.core.Payload;
import net.pingfang.core.codec.Codec;

public class BooleanCodec implements Codec<Boolean> {

	public static BooleanCodec INSTANCE = new BooleanCodec();

	private BooleanCodec() {

	}

	@Override
	public Class<Boolean> forType() {
		return Boolean.class;
	}

	@Override
	public Boolean decode(@Nonnull Payload payload) {
		byte[] data = payload.getBytes(false);

		return data.length > 0 && data[0] > 0;
	}

	@Override
	public Payload encode(Boolean body) {
		return Payload.of(new byte[] { body ? (byte) 1 : 0 });
	}

}
