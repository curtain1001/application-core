package net.pingfang.core.codec.defaults;

import javax.annotation.Nonnull;

import net.pingfang.core.Payload;
import net.pingfang.core.codec.Codec;
import net.pingfang.core.utils.BytesUtils;

public class FloatCodec implements Codec<Float> {

	public static FloatCodec INSTANCE = new FloatCodec();

	private FloatCodec() {

	}

	@Override
	public Class<Float> forType() {
		return Float.class;
	}

	@Override
	public Float decode(@Nonnull Payload payload) {
		return BytesUtils.beToFloat(payload.getBytes(false));
	}

	@Override
	public Payload encode(Float body) {
		return Payload.of(BytesUtils.floatToBe(body));
	}

}
