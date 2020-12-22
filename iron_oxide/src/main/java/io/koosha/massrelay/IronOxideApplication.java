package io.koosha.massrelay;

import io.koosha.massrelay.iron.YIronOxideAppRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IronOxideApplication {

    public static void main(final String... args) {
        SpringApplication.run(IronOxideApplication.class, args)
                         .getBean(YIronOxideAppRunner.class)
                         .run();
    }

}
