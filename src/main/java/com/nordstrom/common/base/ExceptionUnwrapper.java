package com.nordstrom.common.base;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This utility class provides methods for extracting the contents of "wrapped" exceptions.
 */
public class ExceptionUnwrapper {

    private static final Set<Class<? extends Exception>> unwrappable = 
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(RuntimeException.class, InvocationTargetException.class)));

    private ExceptionUnwrapper() {
        throw new AssertionError("ExceptionUnwrapper is a static utility class that cannot be instantiated.");
    }

    /**
     * Unwrap the specified exception.
     * <p>
     * <b>NOTE</b>: This method unwraps the exception chain until it encounters either a non-wrapper exception or an
     * exception with no specified cause. The unwrapped exception is returned.
     * 
     * @param throwable exception to be unwrapped
     * @return unwrapped exception
     */
    public static Throwable unwrap(Throwable throwable) {
        return unwrap(throwable, null);
    }

    /**
     * Unwrap the specified exception, optionally retaining wrapper messages.
     * <p>
     * <b>NOTE</b>: This method unwraps the exception chain until it encounters either a non-wrapper exception or an
     * exception with no specified cause. The unwrapped exception is returned.
     * 
     * @param throwable exception to be unwrapped
     * @param builder to compile wrapper messages (may be 'null')
     * @return unwrapped exception
     */
    public static Throwable unwrap(Throwable throwable, StringBuilder builder) {
        Throwable thrown = throwable;
        if (thrown != null) {
            while (unwrappable.contains(thrown.getClass())) {
                Throwable unwrapped = thrown.getCause();
                if (unwrapped == null) break;
                if (builder != null) builder.append(thrown.getMessage()).append(" -> ");
                thrown = unwrapped;
            }
        }
        return thrown;
    }

}
