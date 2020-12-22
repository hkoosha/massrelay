package io.koosha.massrelay;

import io.koosha.massrelay.aluminum.base.StopManager;
import io.koosha.massrelay.beryllium.BerylliumAppRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import static io.koosha.massrelay.aluminum.base.Util.delayedAction;

@SpringBootApplication
public class BerylliumNitrateApplication {

    public static void main(final String... args) {
        if (args != null && args.length > 0) {
            PasswordTool.main(args);
            return;
        }

        final ConfigurableApplicationContext ctx =
            SpringApplication.run(BerylliumNitrateApplication.class, args);

        final BerylliumAppRunner appRunner = ctx.getBean(BerylliumAppRunner.class);
        final StopManager stopManager = ctx.getBean(StopManager.class);

        appRunner.run();

        stopManager.register(
            appRunner::stop,
            ctx::stop,
            delayedAction(
                BerylliumNitrateApplication.class.getSimpleName(),
                3000,
                () -> System.exit(0))
        );
    }

}
