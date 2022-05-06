package net.pingfang.core.codec;

import javax.annotation.Nonnull;
import net.pingfang.core.Payload;

public interface Decoder<T> {

    Class<T> forType();

    T decode(@Nonnull Payload payload);

    default boolean isDecodeFrom(Object nativeObject){
        if(nativeObject==null){
            return false;
        }
        return forType().isInstance(nativeObject);
    }
}
