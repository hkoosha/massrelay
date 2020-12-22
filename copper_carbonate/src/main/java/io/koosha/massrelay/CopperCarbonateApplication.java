package io.koosha.massrelay;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.copper.err.StoringErrorServiceImpl;
import io.koosha.massrelay.aluminum.base.StopManager;
import io.koosha.massrelay.copper.CopperCarbonateAppRunner;
import io.koosha.massrelay.copper.gui.GuiContainer;
import io.koosha.massrelay.copper.gui.XInjekt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static io.koosha.massrelay.aluminum.base.Util.delayedAction;

@SpringBootApplication
public class CopperCarbonateApplication {

    public static final String VERSION = "2.0.4";

    public static void main(final String... args) throws IOException {

        final ConfigurableApplicationContext ctx =
            SpringApplication.run(CopperCarbonateApplication.class, args);

        final CopperCarbonateAppRunner appRunner = ctx.getBean(CopperCarbonateAppRunner.class);

        appRunner.run();

        ctx.getBean(StopManager.class).register(
            appRunner::stop,
            ctx::stop,
            delayedAction(CopperCarbonateApplication.class.getSimpleName(),
                3000, () -> System.exit(0))
        );

        if (ctx.getBean(Konfiguration.class).bool("gui.enabled").v()) {
            Rrr.setERROR_SERVICE(new StoringErrorServiceImpl());
            final Resource fxml = ctx.getResource("classpath:Gui.fxml");
            GuiContainer.launch(ctx.getBean(XInjekt.class), fxml.getURL(), args);
        }
    }

}
