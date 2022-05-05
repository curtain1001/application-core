package net.pingfang.core.things;

import net.pingfang.core.message.ThingMessage;
import reactor.core.publisher.Flux;

public interface ThingRpcSupport {

	Flux<? extends ThingMessage> call(ThingMessage message);

}
