package net.pingfang.core.value;

import lombok.AllArgsConstructor;
import net.pingfang.core.utils.JsonUtils;

@AllArgsConstructor(staticName = "of")
class SimpleValue implements Value {

    private final Object nativeValue;

    @Override
    public Object get() {
        return nativeValue;
    }

    @Override
    public <T> T as(Class<T> type) {
        if (nativeValue == null) {
            return null;
        }
        if(type.isInstance(nativeValue)){
            return (T)nativeValue;
        }
        return JsonUtils.convert(nativeValue,type);
    }
}
