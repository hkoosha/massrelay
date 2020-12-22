package io.koosha.massrelay.aluminum.base;

import io.koosha.massrelay.aluminum.base.time.Now;

public final class TaskGuard {

    private final Object LOCK = new Object();

    private final Now now;
    private final long timeout;

    private long lastCheck;

    public TaskGuard(final Now now,
                     final long timeout) {
        this.timeout = timeout;
        this.lastCheck = -this.timeout;
        this.now = now;
    }

    public void start() {
        synchronized (LOCK) {
            if (this.isLoop())
                throw new IllegalStateException("already connected");
            this.lastCheck = this.now.millis();
        }
    }

    public void stop() {
        synchronized (LOCK) {
            this.lastCheck = -this.timeout;
        }
    }

    public void reset() {
        synchronized (LOCK) {
            this.lastCheck = this.now.millis();
        }
    }

    public boolean isLoop() {
        synchronized (LOCK) {
            return (this.now.millis() - this.lastCheck) < timeout;
        }
    }

}
