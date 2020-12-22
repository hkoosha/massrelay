package io.koosha.massrelay.aluminum.base.value;

import io.koosha.konfiguration.K;

import java.util.Objects;

public final class Timeout {

    private final long def;

    private final K<Long> general;
    private final K<Long> leftRead;
    private final K<Long> leftWrite;

    public Timeout(final long def,
                   final K<Long> general,
                   final K<Long> leftRead,
                   final K<Long> leftWrite) {
        this.def = def;
        this.general = general;
        this.leftRead = leftRead;
        this.leftWrite = leftWrite;
    }


    public long getGeneral() {
        return this.general.v(this.def);
    }

    public long getLeftRead() {
        return this.leftRead.v(this.def);
    }

    public long getLeftWrite() {
        return this.leftWrite.v(this.def);
    }


    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Timeout))
            return false;
        final Timeout other = (Timeout) o;
        if (this.def != other.def)
            return false;
        if (!Objects.equals(this.general, other.general))
            return false;
        if (!Objects.equals(this.leftRead, other.leftRead))
            return false;
        return Objects.equals(this.leftWrite, other.leftWrite);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = PRIME + (int) (this.def >>> 32 ^ this.def);
        result = result * PRIME + (this.general == null ? 43 : this.general.hashCode());
        result = result * PRIME + (this.leftRead == null ? 43 : this.leftRead.hashCode());
        result = result * PRIME + (this.leftWrite == null ? 43 : this.leftWrite.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Timeout(def=" + this.def +
            ", general=" + this.general +
            ", leftRead=" + this.leftRead +
            ", leftWrite=" + this.leftWrite +
            ")";
    }

}
