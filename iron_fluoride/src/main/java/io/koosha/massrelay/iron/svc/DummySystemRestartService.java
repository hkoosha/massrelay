package io.koosha.massrelay.iron.svc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummySystemRestartService implements SystemRestartService {

    private static final Logger log = LoggerFactory.getLogger(DummySystemRestartService.class);

    @Override
    public void exec() {
        log.warn("supposedly re-starting system");
    }

}
