package io.koosha.massrelay.copper.svc;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.HostAndPort;
import io.koosha.massrelay.aluminum.base.value.Funcode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

import static io.koosha.nettyfunctional.NettyFunc.close;

@Singleton
@Component
public final class GlobalStateManager {

    private static final Logger log = LoggerFactory.getLogger(GlobalStateManager.class);

    private final Object LOCK = new Object();

    private final EventBus bus;

    private Channel rightChannel = null;
    private ChannelFuture rightInProgress;
    private Channel leftChannel = null;
    private String requestedClient = null;
    private Funcode funcode = Funcode.UNKNOWN;
    private HostAndPort target = null;
    private long thereTime = -1;
    private long rebootTime = -1;

    @Inject
    public GlobalStateManager(final EventBus bus) {
        bus.register(this);
        this.bus = bus;
    }

    @Subscribe
    void published(final Event event) {
        switch (event) {
            case LINE_DISCONNECTED:
            case RIGHT_DISCONNECTED:
            case KILL:
                synchronized (LOCK) {
                    close(this.rightChannel);
                    close(this.leftChannel);
                    this.rightChannel = null;
                    this.leftChannel = null;
                    if (rightInProgress != null) {
                        final ChannelFuture cf = rightInProgress.channel().close();
                        cf.awaitUninterruptibly(5_000L);
                        cf.addListener(future -> {
                            if (!future.isSuccess())
                                log.error("could not close in progress right channel", future.cause());
                            bus.post(Event.KILLED);
                        });
                    }
                    else {
                        bus.post(Event.KILLED);
                    }
                }
                break;

            case LEFT_DISCONNECT:
                synchronized (LOCK) {
                    this.leftChannel = null;
                }
                break;
        }
    }


    // ============= RIGHT

    public void setRightInProgress(@NonNull final ChannelFuture v) {
        Objects.requireNonNull(v);
        synchronized (LOCK) {
            this.rightInProgress = v;
        }
    }

    public Channel getRightChannel() {
        synchronized (LOCK) {
            return this.rightChannel;
        }
    }

    public void setRightChannel(@NonNull final Channel v) {
        Objects.requireNonNull(v);
        synchronized (LOCK) {
            this.rightChannel = v;
        }
    }

    public boolean hasViableRightChannel() {
        synchronized (LOCK) {
            return this.rightChannel != null && this.rightChannel.isOpen();
        }
    }


    // ============= LEFT

    public void setLeftChannel(@NonNull final Channel v) {
        Objects.requireNonNull(v);
        synchronized (LOCK) {
            this.leftChannel = v;
        }
    }

    public boolean hasLeftChannel() {
        synchronized (LOCK) {
            return this.leftChannel != null && this.leftChannel.isOpen();
        }
    }


    // ============= REQUEST

    public String getEndpoint() {
        synchronized (LOCK) {
            return this.requestedClient;
        }
    }

    public void setEndpoint(final String activeClient) {
        synchronized (LOCK) {
            this.requestedClient = activeClient;
        }
    }

    public void funcode(final Funcode funcode) {
        synchronized (LOCK) {
            this.funcode = funcode;
        }
    }

    public Funcode funcode() {
        synchronized (LOCK) {
            return funcode;
        }
    }

    public void target(final HostAndPort target) {
        synchronized (LOCK) {
            this.target = target;
        }
    }

    public HostAndPort target() {
        synchronized (LOCK) {
            return target;
        }
    }

    public void setThereTime(final long l) {
        this.thereTime = l;
    }

    public void setRebootTime(final long l) {
        this.rebootTime = l;
    }


    public String toString() {
        return "GlobalStateManager(bus=" + this.bus +
            ", rightChannel=" + this.getRightChannel() +
            ", rightInProgress=" + this.rightInProgress +
            ", leftChannel=" + this.leftChannel +
            ", requestedClient=" + this.requestedClient +
            ", funcode=" + this.funcode +
            ", target=" + this.target +
            ", thereTime=" + this.thereTime +
            ", rebootTime=" + this.rebootTime +
            ")";
    }

}
