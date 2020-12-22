package io.koosha.massrelay;

import io.koosha.massrelay.aluminum.base.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PasswordTool {

    private static final Logger log = LoggerFactory.getLogger(PasswordTool.class);

    // TODO do not hardcode 4
    private final PasswordEncoder enc = PasswordEncoder.bcrypt(4);

    public static void main(final String... args) {
        if (!new PasswordTool().exec(args))
            System.exit(1);
    }

    private boolean exec(final String... args) {
        if (args.length == 0) {
            log.error("no args given, expected 1 or 2");
            return false;
        }
        if (args.length > 2) {
            log.error("too many args given, expected 1 or 2");
            return false;
        }

        if (args.length == 1) {
            log.info("\n[{}]\n{}", args[0], enc.encode(args[0]));
        }
        else {
            if (enc.matches(args[0], args[1])) {
                log.info("matches");
            }
            else {
                log.error("\n[{}]\n[{}]\n[{}]", args[0], enc.encode(args[0]), args[1]);
                log.error("no match");
                return false;
            }
        }

        return true;
    }

}
