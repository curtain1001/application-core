package net.pingfang.core.codec;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.cache.Caches;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;

@SuppressWarnings("all")
@Slf4j
public final class Codecs {

    private static Map<ResolvableType, Codec<?>> mapping = Caches.newCache();

    private static List<CodecsSupport> allCodec = new CopyOnWriteArrayList<>();

    static {
        ServiceLoader
                .load(CodecsSupport.class)
                .forEach(allCodec::add);

        allCodec.sort(Comparator.comparingInt(CodecsSupport::getOrder));
    }

    public static final void register(CodecsSupport support) {
        allCodec.add(support);
        allCodec.sort(Comparator.comparingInt(CodecsSupport::getOrder));
    }

    @Nonnull
    private static Codec<?> resolve(ResolvableType target) {
        for (CodecsSupport support : allCodec) {
            Optional<Codec<?>> lookup = (Optional) support.lookup(target);

            if (lookup.isPresent()) {
                log.debug("lookup codec [{}] for [{}]", lookup.get(), target);
                return lookup.get();
            }
        }
        throw new UnsupportedOperationException("unsupported codec for " + target);
    }

    public static <T> Codec<T> lookup(@Nonnull Class<? extends T> target) {
        return lookup(ResolvableType.forType(target));
    }

    public static <T> Codec<T> lookup(ResolvableType type) {
        if (Publisher.class.isAssignableFrom(type.toClass())) {
            type = type.getGeneric(0);
        }
        return (Codec<T>) mapping.computeIfAbsent(type,Codecs::resolve);
    }

}
