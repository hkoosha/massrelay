package io.koosha.massrelay.aluminum.base.func;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CronTask implements Action {

    // One task at a time.
    private static final Object LOCK = new Object();
    private final Logger log = LoggerFactory.getLogger(getClass());
    private boolean consumed = false;

    public static CronTask of(final ActionE action) {
        return new CronTask() {
            @Override
            protected void run() throws Exception {
                action.exec();
            }
        };
    }

    @Override
    public final void exec() {
        synchronized (LOCK) {
            if (consumed)
                throw new IllegalStateException("task is already run");
            consumed = true;

            try {
                run();
            }
            catch (final Exception e) {
                log.error("failed to run task {}: {}",
                    getClass().getSimpleName(),
                    e.getMessage(),
                    e);
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected abstract void run() throws Exception;

}
