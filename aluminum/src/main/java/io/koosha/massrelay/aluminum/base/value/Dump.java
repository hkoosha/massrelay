package io.koosha.massrelay.aluminum.base.value;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.Konfiguration;

import java.util.Objects;

public final class Dump {

    private final boolean def;

    private final K<Boolean> left;
    private final K<Boolean> right;
    private final K<Boolean> serial;

    private Dump(final boolean def,
                 final K<Boolean> left,
                 final K<Boolean> right,
                 final K<Boolean> serial) {
        this.def = def;
        this.left = left;
        this.right = right;
        this.serial = serial;
    }


    public static Dump create(final Konfiguration k,
                              final boolean def) {
        return new Dump(
            def,
            k.bool("left"),
            k.bool("right"),
            k.bool("serial")
        );
    }


    public boolean left() {
        return this.left.v(this.def);
    }

    public boolean right() {
        return this.right.v(this.def);
    }

    public boolean serial() {
        return this.serial.v(this.def);
    }


    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof final Dump other))
            return false;
        if (this.def != other.def)
            return false;
        if (!Objects.equals(this.left, other.left))
            return false;
        if (!Objects.equals(this.right, other.right))
            return false;
        return Objects.equals(this.serial, other.serial);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.def ? 79 : 97);
        result = result * PRIME + (this.left == null ? 43 : this.left.hashCode());
        result = result * PRIME + (this.right == null ? 43 : this.right.hashCode());
        result = result * PRIME + (this.serial == null ? 43 : this.serial.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Dump(def=" + this.def +
            ", left=" + this.left +
            ", right=" + this.right +
            ", serial=" + this.serial +
            ")";
    }

}
