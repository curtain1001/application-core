package net.pingfang.core.value;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.pingfang.core.utils.JsonUtils;
import org.apache.commons.collections.MapUtils;

@AllArgsConstructor(staticName = "of")
class SimpleValues implements Values {

    @NonNull
    private final Map<String, Object> values;

    @Override
    public Map<String, Object> getAllValues() {
        return new HashMap<>(values);
    }

    @Override
    public Optional<Value> getValue(String key) {
        return Optional
                .ofNullable(key)
                .map(values::get)
                .map(Value::simple);
    }

    @Override
    public Values merge(Values source) {
        Map<String, Object> merged = new HashMap<>();
        merged.putAll(this.values);
        merged.putAll(source.getAllValues());
        return Values.of(merged);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public Set<String> getNonExistentKeys(Collection<String> keys) {
        return keys
                .stream()
                .filter(has -> !values.containsKey(has))
                .collect(Collectors.toSet());
    }

    @Override
    public String getString(String key, Supplier<String> defaultValue) {
        if (MapUtils.isEmpty(values)) {
            return defaultValue.get();
        }
        Object val = values.get(key);
        if (val == null) {
            return defaultValue.get();
        }
        return String.valueOf(val);
    }

    @Override
    public Number getNumber(String key, Supplier<Number> defaultValue) {
        if (MapUtils.isEmpty(values)) {
            return defaultValue.get();
        }
        Object val = values.get(key);
        if (val == null) {
            return defaultValue.get();
        }
        if(val instanceof Number){
            return ((Number) val);
        }
        return JsonUtils.convert(val,Number.class);
    }
}
