package io.koosha.massrelay.aluminum.base.time;

import org.springframework.stereotype.Component;

import javax.inject.Singleton;

@Component
@Singleton
public final class SystemNow implements Now {

    @Override
    public long millis() {
        return System.currentTimeMillis();
    }

}
