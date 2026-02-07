package dev.wolfieboy09.researchtree.api;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class RTUtil {
    /**
     * Applies the given {@link Consumer} to the provided object if it is not {@code null}
    */
    public static <T> void callNotNull(@Nullable T object, Consumer<T> consumer) {
        if (object != null) {
            consumer.accept(object);
        }
    }
}
