package io.koosha.massrelay.copper.err;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

final class DummyErrService implements ErrorService {

    private static final Logger log = LoggerFactory.getLogger(DummyErrService.class);

    @Override
    public List<Err> get(boolean clear, ErrType maxLevel) {
        log.info("dErrService: read({}, {})", clear, maxLevel);
        return Collections.emptyList();
    }

    @Override
    public void error(String msg, Object... args) {
    }

    @Override
    public void warn(String msg, Object... args) {
    }

    @Override
    public void info(String msg, Object... args) {
    }

    @Override
    public void debug(String msg, Object... args) {
    }

}
