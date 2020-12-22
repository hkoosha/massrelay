package io.koosha.massrelay.iron.svc;

import io.koosha.konfiguration.DummyV;
import io.koosha.konfiguration.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class SystemRestartServiceBySudoCommand implements SystemRestartService {

    private static final Logger log = LoggerFactory.getLogger(SystemRestartServiceBySudoCommand.class);

    private static final String COMMAND = "sudo reboot";

    private final K<Boolean> enabled;

    public SystemRestartServiceBySudoCommand() {
        this(DummyV.true_());
    }

    public SystemRestartServiceBySudoCommand(final K<Boolean> enabled) {
        this.enabled = enabled;
    }

    @Override
    public void exec() {
        if (!enabled.v(true))
            return;

        log.warn("re-starting system");
        try {
            Runtime.getRuntime().exec(COMMAND);
        }
        catch (final IOException e) {
            log.error("re-starting system failed:", e);
            throw new UncheckedIOException(e);
        }
    }

}
