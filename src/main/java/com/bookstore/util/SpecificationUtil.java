package com.bookstore.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

@UtilityClass
public class SpecificationUtil {
    public <T> Specification<T> alwaysTrue() {
        return Specification.where(null);
    }

    public static <T, V> Specification<T> applyIfPresent(Function<V, Specification<T>> fn, V value) {
        return Objects.isNull(value) ? null : fn.apply(value);
    }

    public static <T, E> Specification<T> applyIfNotEmpty(Function<Collection<E>, Specification<T>> fn,
                                                          Collection<E> col) {
        return col.isEmpty() ? null : fn.apply(col);
    }
}
