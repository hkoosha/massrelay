package io.koosha.massrelay.copper.handler;

import com.google.common.eventbus.EventBus;
import com.google.common.net.HostAndPort;
import io.koosha.nettyfunctional.NettyFunc;
import io.koosha.nettyfunctional.hook.InboundTransformer;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.nett.AwaitFixedLengthBytes;
import io.koosha.massrelay.aluminum.base.value.Bummer;
import io.koosha.massrelay.aluminum.base.value.Funcodes;
import io.koosha.massrelay.copper.svc.Event;
import io.koosha.massrelay.copper.svc.GlobalStateManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.function.Function;

import static io.koosha.nettyfunctional.NettyFunc.write;
import static io.koosha.massrelay.aluminum.base.Util.encode;


@ChannelHandler.Sharable
@Singleton
@Component
public final class Right1_OnLineAcquired extends InboundTransformer<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(Right1_OnLineAcquired.class);

    private static final int VERSION = 0;

    // now + reboot + version + ok
    private static final int FRAME_LEN = 8 + 8 + 4 + 1;

    private final Provider<Right2_OnArgSent> onArgSent;
    private final Provider<LengthFieldPrepender> framer;
    private final GlobalStateManager gsm;
    private final EventBus bus;

    @Inject
    public Right1_OnLineAcquired(final Provider<Right2_OnArgSent> onArgSent,
                                 final Provider<LengthFieldPrepender> framer,
                                 final GlobalStateManager gsm, EventBus bus) {
        this.onArgSent = onArgSent;
        this.framer = framer;
        this.gsm = gsm;
        this.bus = bus;
    }

    public static ChannelHandler awaiter() {
        return new AwaitFixedLengthBytes(FRAME_LEN);
    }

    @Override
    protected Object read1(final ChannelHandlerContext ctx,
                           final ByteBuf msg) {
        final Function<String, Void> fin = s -> {
            bus.post(Event.LINE_DISCONNECTED);
            bus.post(Event.RIGHT_DISCONNECTED);
            final Object[] args = new Object[]{};
            log.warn(s, args);
            Rrr.warn(s, args);
            NettyFunc.closeN(ctx);
            return null;
        };

        assert msg.readableBytes() == FRAME_LEN;

        final int version = msg.readInt();
        if (version != VERSION) {
            log.warn("incompatible version: {}", version);
            Rrr.warn("incompatible version: {}", version);
            ctx.close();
            return null;
        }

        if (Bummer.find(msg.readByte()) != Bummer.OK)
            return fin.apply("line error");

        gsm.setThereTime(msg.readLong());
        gsm.setRebootTime(msg.readLong());
        bus.post(Event.THERE_TIME);
        bus.post(Event.REBOOT_TIME);

        if (gsm.funcode() == Funcodes.TCP) {
            log.info("EVENT :: line acquired, sending target");
            Rrr.info("EVENT :: line acquired, sending target");
            ctx.pipeline().addBefore(ctx.name(), null, framer.get());
            ctx.pipeline().addBefore(ctx.name(), null, onArgSent.get());
            ctx.pipeline().remove(this);
            final HostAndPort value = gsm.target();
            final ByteBuf say = encode(value.getHost()).writeInt(value.hasPort() ? value.getPort() : -1);
            write(ctx, say, () -> {
                Rrr.info("EVENT :: tcp target sent");
                log.info("EVENT :: tcp target sent");
            }, throwable -> {
                log.error("EVENT :: could not send arg", throwable);
                fin.apply("EVENT :: line error");
            });
        }
        else {
            ctx.pipeline().remove(this);
            log.info("EVENT :: line acquired");
            Rrr.info("EVENT :: line acquired");
            gsm.setRightChannel(ctx.channel());
            bus.post(Event.LINE_CONNECTED);
        }

        return null;
    }

}
