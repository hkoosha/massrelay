package io.koosha.massrelay.aluminum.base.value;

public final class Result<A> {

    private static final StackTraceElement[] el0 = new StackTraceElement[0];
    private static final Throwable GENERIC = new ReadOnlyRuntimeException("");

    private final A a;
    private final Throwable cause;

    // ------------------------------------------------------------------------

    private Result(final A a,
                   final Throwable cause) {
        this.a = a;
        this.cause = cause;
    }

    public static <A> Result<A> ok(final A a) {
        return new Result<>(a, null);
    }

    public static <A> Result<A> fail(final Throwable cause) {
        if (cause == null)
            throw new NullPointerException("no failure cause specified for failed result");
        return new Result<>(null, cause);
    }

    public static <A> Result<A> fail() {
        return new Result<>(null, GENERIC);
    }

    public static <A> Result<A> fail(final String error) {
        return new Result<>(null, new ReadOnlyRuntimeException(error));
    }

    public boolean isSuccess() {
        return this.cause == null;
    }

    public boolean isFailure() {
        return this.cause != null;
    }

    public A get() {
        if (this.isFailure())
            throw new IllegalStateException("asked for result on a failed result");
        return this.a;
    }

    public Throwable getCause() {
        return this.cause;
    }


    private static final class ReadOnlyRuntimeException extends RuntimeException {

        private ReadOnlyRuntimeException(final String message) {
            super(message);
            super.setStackTrace(el0);
        }

        @Override
        public void setStackTrace(final StackTraceElement[] stackTrace) {
        }

    }

}
