package io.koosha.massrelay.copper.err;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public final class Rrr {

    private static final Logger log = LoggerFactory.getLogger(Rrr.class);

    private static ErrorService ERROR_SERVICE = new DummyErrService();

    private Rrr() {
    }


    public static List<Err> get(final ErrType max) {
        return ERROR_SERVICE.get(true, max);
    }

    public static boolean debug(final String msg, final Object... args) {
        try {
            ERROR_SERVICE.debug(msg, args);
        }
        catch (Exception e) {
            log.warn("error erroring on error service", e);
        }
        return true;
    }

    public static boolean info(final String msg, final Object... args) {
        try {
            ERROR_SERVICE.info(msg, args);
        }
        catch (Exception e) {
            log.warn("error erroring on error service", e);
        }
        return true;
    }

    public static boolean warn(final String msg, final Object... args) {
        try {
            ERROR_SERVICE.warn(msg, args);
        }
        catch (Exception e) {
            log.warn("error erroring on error service", e);
        }
        return false;
    }

    public static boolean error(final String msg, final Object... args) {
        try {
            ERROR_SERVICE.error(msg, args);
        }
        catch (Exception e) {
            log.warn("error erroring on error service", e);
        }
        return false;
    }

    public static void setERROR_SERVICE(final ErrorService ERROR_SERVICE) {
        Rrr.ERROR_SERVICE = Objects.requireNonNull(ERROR_SERVICE, "ERROR_SERVICE");
    }

}
