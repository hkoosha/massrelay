package io.koosha.massrelay.aluminum.base.value;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Funcode {

    private static final Object LOCK = new Object();

    private static final Set<Funcode> all = new HashSet<>();

    public static final Funcode UNKNOWN = unknown();
    private final byte raw;
    private final String name;

    private Funcode(final byte raw,
                    final String name) {
        this.raw = raw;
        this.name = name;
    }

    // ------------------------------------------------------------

    private static Funcode unknown() {
        final Funcode f = new Funcode((byte) 255, "UNKNOWN");
        all.add(f);
        return f;
    }

    public static Funcode define(final int raw,
                                 final String name) {
        if (raw < 0 || raw > 254)
            throw new IllegalArgumentException("raw value out of range 0~254");

        final byte rb = (byte) raw;

        synchronized (LOCK) {
            for (final Funcode funcode : all)
                if (funcode.raw == rb) {
                    if (!Objects.equals(funcode.name, name))
                        throw new IllegalStateException(
                            "other funcode (different raw value) with this name exists: " + name);
                    return funcode;
                }
            final Funcode f = new Funcode(rb, name);
            all.add(f);
            return f;
        }
    }

    public static Funcode find(final int raw) {
        if (raw < 0 || raw > 255)
            throw new IllegalArgumentException("raw value out of range 0~255");

        final byte rb = (byte) raw;

        synchronized (LOCK) {
            for (final Funcode funcode : all)
                if (funcode.raw() == rb)
                    return funcode;
            return UNKNOWN;
        }
    }

    public byte raw() {
        return this.raw;
    }

    public String name() {
        return this.name;
    }


    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Funcode))
            return false;
        return this.raw == ((Funcode) o).raw;
    }

    @Override
    public int hashCode() {
        return this.raw;
    }

    @Override
    public String toString() {
        return "Funcode[" + "raw=" + raw + ", name=" + name + ']';
    }

}
