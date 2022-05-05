package net.pingfang.core.metadata;

public interface Converter<T> {

	T convert(Object value);
}
