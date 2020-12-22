package io.koosha.massrelay.copper.handler;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.HostAndPort;
import io.koosha.massrelay.aluminum.base.TaskGuard;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.qualify.Right;
import io.koosha.massrelay.aluminum.base.value.Secret;
import io.koosha.massrelay.aluminum.base.value.Funcode;
import io.koosha.massrelay.copper.svc.Event;
import io.koosha.massrelay.copper.svc.GlobalStateManager;
import io.koosha.nettyfunctional.nettyfunctions.Consumer2;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.koosha.massrelay.aluminum.base.Util.unpool;
import static io.koosha.massrelay.aluminum.base.Util.encode;
import static io.koosha.massrelay.aluminum.base.Util.causeMsg;
import static io.koosha.nettyfunctional.NettyFunc.connect;
import static io.koosha.nettyfunctional.NettyFunc.writer;

@Singleton
@Component
public final class ServerDialerService {

    private static final Logger log = LoggerFactory.getLogger(ServerDialerService.class);

    private static final int VERSION = 0;

    private final EventBus bus;
    private final Supplier<String> clientId;
    private final Consumer<ChannelFuture> setInProgress;
    private final HostAndPort right;
    private final Bootstrap bootstrap;
    private final TaskGuard taskGuard;

    @Inject
    public ServerDialerService(final EventBus bus,
                               final GlobalStateManager gsm,
                               @Right final HostAndPort right,
                               @Right final Bootstrap bootstrap,
                               final TaskGuard taskGuard) {
        this.bus = bus;
        this.clientId = gsm::getEndpoint;
        this.setInProgress = gsm::setRightInProgress;
        this.right = right;
        this.bootstrap = bootstrap;
        this.taskGuard = taskGuard;
        bus.register(this);
    }

    @Subscribe
    void published(final Event event) {
        switch (event) {
            case LINE_DISCONNECTED:
            case RIGHT_DISCONNECTED:
            case KILLED:
                taskGuard.stop();
                break;

            default:
                // To make spotbugs happy :|
                break;
        }
    }

    public void dial(final Secret secret,
                     final Funcode funcode,
                     final HostAndPort remote) {
        if (taskGuard.isLoop()) {
            log.warn("already connecting");
            Rrr.warn("already connecting");
            return;
        }

        log.info("dialing server -> {}", right);
        taskGuard.start();
        bus.post(Event.RIGHT_CONNECTING);

        final Consumer2<Throwable> fail = t -> {
            Rrr.error("connecting to server failed: {}", causeMsg(t));
            log.error("connecting to server failed: {}", causeMsg(t));
            bus.post(Event.RIGHT_DISCONNECTED);
            bus.post(Event.LINE_DISCONNECTED);
            taskGuard.stop();
        };

        final String clientId = this.clientId.get();
        if (clientId == null || clientId.isEmpty()) {
            fail.accept(new RuntimeException("client id not set"));
            return;
        }

        final ByteBuf say = unpool(VERSION)
            .writeBytes(encode(secret.id(), secret.hash()))
            .writeByte(funcode.raw())
            .writeBytes(clientId.getBytes(StandardCharsets.UTF_8));

        final Bootstrap conn = this.bootstrap.clone().remoteAddress(remote.getHost(), remote.getPort());
        final AtomicReference<ChannelFuture> cfEr = new AtomicReference<>(null);
        log.info("EVENT :: dialing");
        final ChannelFuture cf = connect(conn, c -> {
            log.info("EVENT :: authenticating");
            bus.post(Event.RIGHT_AUTHENTICATING);
            setInProgress.accept(cfEr.get());
            writer(c, say, fail);
        }, fail);
        cfEr.set(cf);
        setInProgress.accept(cf);
    }

}
