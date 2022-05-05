package net.pingfang.core.message.codec;

import reactor.core.publisher.Mono;

/**
 * @since 1.0
 **/
public interface TransportDeviceMessageCodec {

	Transport getSupportTransport();

	Mono<EncodedMessage> encode(MessageEncodeContext context);

	Mono<DeviceMessage> decode(MessageDecodeContext context);
}
