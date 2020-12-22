package io.koosha.massrelay.aluminum;

import com.google.common.net.HostAndPort;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.massrelay.aluminum.base.StopManager;
import io.koosha.massrelay.aluminum.base.TaskGuard;
import io.koosha.massrelay.aluminum.base.file.CtxedFileService;
import io.koosha.massrelay.aluminum.base.file.FileService;
import io.koosha.massrelay.aluminum.base.qualify.Left;
import io.koosha.massrelay.aluminum.base.qualify.Right;
import io.koosha.massrelay.aluminum.base.qualify.Server;
import io.koosha.massrelay.aluminum.base.security.PasswordEncoder;
import io.koosha.massrelay.aluminum.base.time.Now;
import io.koosha.massrelay.aluminum.base.time.SystemNow;
import io.koosha.massrelay.aluminum.base.value.Dump;
import io.koosha.massrelay.aluminum.base.value.Secret;
import io.koosha.massrelay.aluminum.base.value.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class ZBaseProvider {

    public static final String PROFILE_DEV = "development";
    public static final String PROFILE_PROD = "production";

    private static final int BCRYPT_STRENGTH = 4;
    private static final boolean DUMP = false;
    private static final long TIMEOUT = 180_000;

    private static String split(final String str,
                                final int index) {
        final String[] split = str.split(":");
        if (split.length <= index)
            return "";

        final String ret = split[index];
        return ret.isEmpty() ? "" : ret;
    }


    @Singleton
    @Bean
    Now now() {
        return new SystemNow();
    }

    @Server
    @Lazy
    @Singleton
    @Bean
    ExecutorService serverRunner() {
        final int cores = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(cores * 2);
    }

    @Lazy
    @Singleton
    @Bean
    ScheduledExecutorService cron() {
        return Executors.newSingleThreadScheduledExecutor();
    }


    @Lazy
    @Singleton
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoder.bcrypt(BCRYPT_STRENGTH);
    }

    @Lazy
    @Singleton
    @Bean
    StopManager stopManager() {
        return new StopManager();
    }

    @Lazy
    @Singleton
    @Bean
    TaskGuard taskGuard(final Now now,
                        final Timeout timeout) {
        return new TaskGuard(now, timeout.getGeneral());
    }

    @Left
    @Lazy
    @Singleton
    @Bean
    HostAndPort left(final Konfiguration k) {
        return HostAndPort.fromString(k.string("left").v());
    }

    @Right
    @Lazy
    @Singleton
    @Bean
    HostAndPort right(final Konfiguration k) {
        return HostAndPort.fromString(k.string("right").v());
    }

    @Lazy
    @Singleton
    @Bean
    Dump dump(final Konfiguration k) {
        return Dump.create(k.subset("dump"), DUMP);
    }

    @Lazy
    @Singleton
    @Bean
    Timeout timeout(final Konfiguration k) {
        return new Timeout(
            TIMEOUT,
            k.long_("timeout.general"),
            k.long_("timeout.leftRead"),
            k.long_("timeout.leftWrite")
        );
    }

    @Lazy
    @Singleton
    @Bean
    Secret secret(final Konfiguration k) {
        final String secret = k.string("secret").v("");
        if ("N/A".equalsIgnoreCase(secret))
            return new Secret("", "");

        if (secret.isEmpty())
            throw new IllegalArgumentException("empty secret, if this is intended, use the value: n/a");

        final String id = split(secret, 0);
        final String hash = split(secret, 1);

        return new Secret(id, hash);
    }

    // ------------------------------------------------------------------------

    @Lazy
    @Singleton
    @Bean
    FileService fileService() {
        return new CtxedFileService();
    }

}
