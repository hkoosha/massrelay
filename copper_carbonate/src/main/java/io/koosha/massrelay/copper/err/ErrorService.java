package io.koosha.massrelay.copper.err;

import java.util.List;

interface ErrorService {

    List<Err> get(boolean clear, ErrType maxLevel);

    void error(String msg, Object... args);

    void warn(String msg, Object... args);

    void info(String msg, Object... args);

    void debug(String msg, Object... args);

}
