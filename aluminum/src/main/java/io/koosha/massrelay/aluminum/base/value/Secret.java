package io.koosha.massrelay.aluminum.base.value;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class Secret {

    private final String id;

    private final String hash;


    public Secret(final String id,
                  final String hash) {
        this.id = Objects.requireNonNull(id);
        this.hash = Objects.requireNonNull(hash);
    }


    public byte[] id() {
        return this.id.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] hash() {
        return this.hash.getBytes(StandardCharsets.UTF_8);
    }

    public String getId() {
        return this.id;
    }

    public String getHash() {
        return this.hash;
    }


    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Secret))
            return false;
        final Secret other = (Secret) o;
        if (!Objects.equals(this.getId(), other.getId()))
            return false;
        return Objects.equals(this.getHash(), other.getHash());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object id = this.getId();
        result = result * PRIME + id.hashCode();
        final Object hash = this.getHash();
        result = result * PRIME + hash.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Secret(id=" + this.getId() + ", hash=" + this.getHash() + ")";
    }

}
