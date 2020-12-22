package io.koosha.massrelay.copper.err;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

public final class StoringErrorServiceImpl implements ErrorService {

    private static final Logger log = LoggerFactory.getLogger(StoringErrorServiceImpl.class);

    private static final String PATTERN = Pattern.quote("{}");

    private final Object LOCK = new Object();
    private final Map<Long, Err> errors = new HashMap<>();

    private static String format(final String msg,
                                 final Object... args) {
        int index = 0;
        String fix = msg;
        try {
            while (fix.contains("{}") && args.length > index)
                fix = fix.replaceFirst(PATTERN, String.valueOf(args[index++]));
        }
        catch (Exception e) {
            log.warn("error formatting failed: ", e);
            return "";
        }
        return fix;
    }

    private void add(final ErrType type,
                     final String msg,
                     final Object... args) {
        final Err error = new Err(type, format(msg, args));
        synchronized (LOCK) {
            errors.put(error.getId(), error);
        }
    }

    @Override
    public List<Err> get(final boolean clear,
                         final ErrType maxLevel) {
        final Map<Long, Err> copy;
        synchronized (LOCK) {
            copy = new HashMap<>(errors);
        }

        final List<Err> ret = copy.values()
                                  .stream()
                                  .filter(err -> err.getErrType()
                                                    .getLevel() <= maxLevel.getLevel())
                                  .collect(toList());
        if (clear)
            errors.clear();

        return ret;
    }

    @Override
    public void error(final String msg, final Object... args) {
        if (log.isErrorEnabled())
            this.add(ErrType.ERROR, msg, args);
    }

    @Override
    public void warn(final String msg, final Object... args) {
        if (log.isWarnEnabled())
            this.add(ErrType.WARN, msg, args);
    }

    @Override
    public void info(final String msg, final Object... args) {
        if (log.isInfoEnabled())
            this.add(ErrType.INFO, msg, args);
    }

    @Override
    public void debug(final String msg, final Object... args) {
        if (log.isDebugEnabled())
            this.add(ErrType.DEBUG, msg, args);
    }

}
