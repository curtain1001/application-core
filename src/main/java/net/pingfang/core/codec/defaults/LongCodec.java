package net.pingfang.core.codec.defaults;

import javax.annotation.Nonnull;

import net.pingfang.core.Payload;
import net.pingfang.core.codec.Codec;
import net.pingfang.core.utils.BytesUtils;

/**
 * 长整型转换
 */
public class LongCodec implements Codec<Long> {

	public static LongCodec INSTANCE = new LongCodec();

	private LongCodec() {

	}

	@Override
	public Class<Long> forType() {
		return Long.class;
	}

	@Override
	public Long decode(@Nonnull Payload payload) {
		return BytesUtils.beToLong(payload.getBytes(false));
	}

	@Override
	public Payload encode(Long body) {
		return Payload.of(BytesUtils.longToBe(body));
	}

}
