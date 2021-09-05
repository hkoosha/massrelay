package io.koosha.massrelay.copper.err;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class Err {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    private final long timestamp = System.currentTimeMillis();
    private final long id = COUNTER.getAndIncrement();

    private final ErrType errType;
    private final String error;

    public Err(final ErrType errType,
               final String error) {
        this.errType = errType;
        this.error = error;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getId() {
        return this.id;
    }

    public ErrType getErrType() {
        return this.errType;
    }

    public String getError() {
        return this.error;
    }

    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof final Err other))
            return false;
        if (this.getTimestamp() != other.getTimestamp())
            return false;
        if (this.getId() != other.getId())
            return false;
        final Object thisErrType = this.getErrType();
        final Object otherErrType = other.getErrType();
        if (!Objects.equals(thisErrType, otherErrType))
            return false;
        final Object thisError = this.getError();
        final Object otherError = other.getError();
        return Objects.equals(thisError, otherError);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long timestamp = this.getTimestamp();
        result = result * PRIME + (int) (timestamp >>> 32 ^ timestamp);
        final long id = this.getId();
        result = result * PRIME + (int) (id >>> 32 ^ id);
        final Object errType = this.getErrType();
        result = result * PRIME + (errType == null ? 43 : errType.hashCode());
        final Object error = this.getError();
        result = result * PRIME + (error == null ? 43 : error.hashCode());
        return result;
    }

    public String toString() {
        return "Err(timestamp=" + this.getTimestamp() +
            ", id=" + this.getId() +
            ", errType=" + this.getErrType() +
            ", error=" + this.getError() +
            ")";
    }

}
