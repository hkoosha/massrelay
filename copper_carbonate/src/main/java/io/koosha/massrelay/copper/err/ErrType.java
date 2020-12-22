package io.koosha.massrelay.copper.err;

public enum ErrType {

    ERROR(0),
    WARN(1),
    INFO(2),
    DEBUG(3),

    ;

    private final int level;

    ErrType(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }

}
