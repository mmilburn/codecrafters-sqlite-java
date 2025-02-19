package util;

import java.util.function.Supplier;

public class Memoization {
    public static <T> Supplier<T> memoize(Supplier<T> supplier) {
        return new Supplier<>() {
            private T value;
            private boolean computed = false;

            @Override
            public T get() {
                if (!computed) {
                    value = supplier.get();
                    computed = true;
                }
                return value;
            }
        };
    }
}