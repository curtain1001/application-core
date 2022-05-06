package net.pingfang.core.codec;

import java.util.Optional;
import org.springframework.core.ResolvableType;

public interface CodecsSupport {

    <T> Optional<Codec<T>> lookup(ResolvableType type);

    int getOrder();
}
