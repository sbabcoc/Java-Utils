package com.nordstrom.common.base;

/**
 * Throwable created purely for the purposes of reporting a stack trace.
 * 
 * This is not an Error or an Exception and is not expected to be thrown or caught.
 * This <a href='https://blog.vanillajava.blog/2021/12/unusual-java-stacktrace-extends.html'>blog post</a> provided the
 * original implementation.
 * @author <a href='https://github.com/peter-lawrey'>Peter K Lawrey</a>
 */

public class StackTrace extends Throwable {

    private static final long serialVersionUID = -3623586250962214453L;

    public StackTrace() {
        this("stack trace");
    }

    public StackTrace(String message) {
        this(message, null);
    }

    public StackTrace(String message, Throwable cause) {
        super(message + " on " + Thread.currentThread().getName(), cause);
    }

    public static StackTrace forThread(Thread t) {
        if (t == null) return null;

        StackTrace st = new StackTrace(t.toString());
        StackTraceElement[] stackTrace = t.getStackTrace();
        int start = 0;
        
        if (stackTrace.length > 2) {
            if (stackTrace[0].isNativeMethod()) {
                start++;
            }
        }

        if (start > 0) {
            StackTraceElement[] ste2 = new StackTraceElement[stackTrace.length - start];
            System.arraycopy(stackTrace, start, ste2, 0, ste2.length);
            stackTrace = ste2;
        }

        st.setStackTrace(stackTrace);
        return st;
    }
}
