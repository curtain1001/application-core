package net.pingfang.core.codec.defaults;

import javax.annotation.Nonnull;

import net.pingfang.core.Payload;
import net.pingfang.core.codec.Codec;
import net.pingfang.core.utils.BytesUtils;

public class IntegerCodec implements Codec<Integer> {

	public static IntegerCodec INSTANCE = new IntegerCodec();

	private IntegerCodec() {

	}

	@Override
	public Class<Integer> forType() {
		return Integer.class;
	}

	@Override
	public Integer decode(@Nonnull Payload payload) {
		return BytesUtils.beToInt(payload.getBytes(false));
	}

	@Override
	public Payload encode(Integer body) {
		return Payload.of(BytesUtils.intToBe(body));
	}

}
