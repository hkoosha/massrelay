package io.koosha.massrelay.copper.gui;

import io.koosha.massrelay.copper.err.Err;
import io.koosha.massrelay.copper.err.ErrType;
import io.koosha.massrelay.copper.err.Rrr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class ErrorCtrl {

    private static final int MAX_LEN = 80;

    @SuppressWarnings("FieldCanBeLocal")
    private final ScheduledExecutorService bgTask = Executors.newSingleThreadScheduledExecutor();

    private final DateFormat formatter = new SimpleDateFormat("mm:ss");

    ErrorCtrl(final XGuiCtrl g) {
        bgTask.scheduleAtFixedRate(() -> {
            final StringBuilder sb = new StringBuilder();
            final List<Err> err = Rrr.get(ErrType.INFO);
            if (err.isEmpty())
                return;
            for (final Err e : err) {
                sb.append("\n[")
                  .append(formatter.format(new Date(e.getTimestamp())))
                  .append(" - ")
                  .append(e.getErrType())
                  .append("] ");
                final boolean was = e.getError().length() > MAX_LEN;
                String error = e.getError();
                while (error.length() > MAX_LEN) {
                    final String sub = error.substring(0, MAX_LEN);
                    error = error.substring(MAX_LEN);
                    sb.append("\n    -> ")
                      .append(sub);
                }
                if (error.length() > 0) {
                    if (was)
                        sb.append("\n    -> ")
                          .append(error);
                    else
                        sb.append(error);
                }
            }
            g.err0.appendText(sb.toString());
            g.err0.setScrollTop(9999);
        }, 200, 200, TimeUnit.MILLISECONDS);
        g.err0.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1)
                g.err0.clear();
        });
        XGuiCtrl.injekt.getStopManager().register(bgTask::shutdown);
    }

}
