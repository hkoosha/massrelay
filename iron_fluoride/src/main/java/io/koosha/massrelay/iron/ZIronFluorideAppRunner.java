package io.koosha.massrelay.iron;

import io.koosha.konfiguration.Konfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;

import static io.koosha.massrelay.aluminum.base.Util.cron;

@Singleton
@Component
public final class ZIronFluorideAppRunner {

    private static final Logger log = LoggerFactory.getLogger(ZIronFluorideAppRunner.class);

    private final ScheduledExecutorService cron;
    private final Provider<IronDialer> dialTask;
    private final Konfiguration k;

    @Inject
    public ZIronFluorideAppRunner(final ScheduledExecutorService cron,
                                  final Provider<IronDialer> dialTask,
                                  final Konfiguration k) {
        this.cron = cron;
        this.dialTask = dialTask;
        this.k = k;
    }

    public void run() {
        final long every = k.long_("cron").v();
        log.info("registering dial task every {}", every);
        cron(cron, every, dialTask);
    }

}
