package io.koosha.massrelay.beryllium.svc;

import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.aluminum.base.value.Funcode;
import io.koosha.massrelay.aluminum.base.value.Timeout;
import io.koosha.nettyfunctional.NettyFunc;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import static io.koosha.massrelay.aluminum.base.Util.cron;

@SuppressWarnings("WeakerAccess")
// TODO: race on left handler and right handler
@Singleton
@Component
public final class LeftChannelRegistry {

    private static final Logger log = LoggerFactory.getLogger(LeftChannelRegistry.class);

    private final Object LOCK = new Object();
    private final Map<String, Entry> registry = new HashMap<>();

    @Inject
    public LeftChannelRegistry(final ScheduledExecutorService cron,
                               final Timeout timeout) {
        cron(cron, timeout.getGeneral(), this::clean);
    }

    public void clean() {
        synchronized (LOCK) {
            registry.entrySet().removeIf(e -> e.getValue().isDead());
        }
    }

    public void put(final Channel leftChannel,
                    final String rightId,
                    final Funcode funcode) {
        synchronized (LOCK) {
            final String id = leftChannel.attr(Util.ID).get();

            log.info("adding left id={} remote={}, rid={} funcode={}",
                    id, Util.socketAddrInfo(leftChannel.remoteAddress()), rightId, funcode);

            final Entry p = registry.put(id, new Entry(leftChannel, rightId, funcode));
            if (p != null)
                NettyFunc.close(p.leftChannel);
        }
    }

    public Channel getAndRemove(final String leftId) {
        synchronized (LOCK) {
            final Entry entry = registry.remove(leftId);

            log.info("removing left: {} - {}",
                    entry,
                    entry == null ? "?" : Util.socketAddrInfo(entry.leftChannel.remoteAddress()));

            return entry == null || entry.isDead() ? null : entry.leftChannel;
        }
    }

    public Channel getAndRemove(final String rightId,
                                final Funcode funcode) {
        synchronized (LOCK) {
            String id = null;
            for (final Map.Entry<String, Entry> e : registry.entrySet())
                if (e.getValue().funcode == funcode || Objects.equals(e.getValue().rightId, rightId)) {
                    id = e.getKey();
                    break;
                }

            if (id == null)
                return null;

            final Entry ret = registry.remove(id);

            log.info("removing for right id={} remote={}, rid={} funcode={}",
                    id, Util.socketAddrInfo(ret.leftChannel.remoteAddress()), rightId, funcode);

            return ret.isDead() ? null : ret.leftChannel;
        }
    }


    private record Entry(Channel leftChannel,
                         String rightId,
                         Funcode funcode) {

        private boolean isDead() {
            return !this.leftChannel.isOpen();
        }
    }

}
