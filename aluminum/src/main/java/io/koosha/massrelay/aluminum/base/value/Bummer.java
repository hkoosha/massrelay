package io.koosha.massrelay.aluminum.base.value;

public enum Bummer {

    OK(1),
    AUTH_ERROR(2),
    ACCESS_DENIED(3),
    UNKNOWN(Byte.MAX_VALUE);

    private final byte raw;

    Bummer(final int raw) {
        if (raw > 256 || raw < 0)
            throw new IndexOutOfBoundsException("integer can not be decoded into byte: " + raw);
        this.raw = (byte) raw;
    }


    public static Bummer find(final byte raw) {
        for (final Bummer v : Bummer.values())
            if (v.raw == raw)
                return v;
        throw new IllegalArgumentException("no such Bummer: " + raw);
    }

    public byte raw() {
        return this.raw;
    }

}
