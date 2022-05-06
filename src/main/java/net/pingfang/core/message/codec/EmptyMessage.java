package net.pingfang.core.message.codec;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author wangchao
 * @since 1.0.0
 */
public final class EmptyMessage implements EncodedMessage {

	public static final EmptyMessage INSTANCE = new EmptyMessage();

	private EmptyMessage() {
	}

	@Nonnull
	@Override
	public ByteBuf getPayload() {
		return Unpooled.wrappedBuffer(new byte[0]);
	}

	@Override
	public String toString() {
		return "empty message";
	}
}
