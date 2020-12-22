package io.koosha.massrelay.iron;

import org.springframework.stereotype.Component;

import javax.inject.Singleton;

@Singleton
@Component
public final class TimeRemainingToRestartService {

    long millis() {
        return 0;
    }

}
