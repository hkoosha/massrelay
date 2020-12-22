package io.koosha.massrelay.aluminum.base.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.regex.Pattern;


/**
 * Implementation of PasswordEncoder that uses the BCrypt strong hashing
 * function. Clients can optionally supply a "strength" (a.k.a. log rounds in
 * BCrypt) and a SecureRandom instance. The larger the strength parameter the
 * more work will have to be done (exponentially) to hash the passwords. The
 * default value is 10.
 *
 * @author Dave Syer
 */
final class BCryptPasswordEncoder implements PasswordEncoder {

    private static final Logger log = LoggerFactory.getLogger(BCryptPasswordEncoder.class);

    private static final Pattern BCRYPT_PATTERN = Pattern
        .compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");

    private final int strength;

    private final SecureRandom random;

    /**
     * @param strength the log rounds to use, between 4 and 31
     * @param random   the secure random instance to use
     */
    BCryptPasswordEncoder(int strength, SecureRandom random) {
        if (strength != -1 && (strength < BCrypt.MIN_LOG_ROUNDS || strength > BCrypt.MAX_LOG_ROUNDS)) {
            throw new IllegalArgumentException("Bad strength");
        }
        this.strength = strength;
        this.random = random;
    }

    public String encode(CharSequence rawPassword) {
        String salt;
        if (strength > 0) {
            if (random != null) {
                salt = BCrypt.gensalt(strength, random);
            }
            else {
                salt = BCrypt.gensalt(strength);
            }
        }
        else {
            salt = BCrypt.gensalt();
        }
        return BCrypt.hashpw(rawPassword.toString(), salt);
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() == 0) {
            log.warn("Empty encoded password");
            return false;
        }

        if (!BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
            log.warn("Encoded password does not look like BCrypt");
            return false;
        }

        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }

}
