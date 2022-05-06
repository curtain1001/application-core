package net.pingfang.core.codec;

import net.pingfang.core.Payload;

public interface Encoder<T> {

    Payload encode(T body);

}
