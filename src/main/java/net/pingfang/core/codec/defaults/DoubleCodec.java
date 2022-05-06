package net.pingfang.core.codec.defaults;

import javax.annotation.Nonnull;

import net.pingfang.core.Payload;
import net.pingfang.core.codec.Codec;
import net.pingfang.core.utils.BytesUtils;

public class DoubleCodec implements Codec<Double> {

	public static DoubleCodec INSTANCE = new DoubleCodec();

	private DoubleCodec() {

	}

	@Override
	public Class<Double> forType() {
		return Double.class;
	}

	@Override
	public Double decode(@Nonnull Payload payload) {
		return BytesUtils.beToDouble(payload.getBytes(false));
	}

	@Override
	public Payload encode(Double body) {
		return Payload.of(BytesUtils.doubleToBe(body));
	}

}
