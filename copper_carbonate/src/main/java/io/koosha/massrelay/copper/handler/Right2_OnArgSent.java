package io.koosha.massrelay.copper.handler;

import com.google.common.eventbus.EventBus;
import io.koosha.nettyfunctional.NettyFunc;
import io.koosha.nettyfunctional.hook.InboundTransformer;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.value.Bummer;
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
import javax.inject.Singleton;
import java.util.function.Function;

import static io.koosha.massrelay.aluminum.base.Util.ensureExactOneByte;


@ChannelHandler.Sharable
@Singleton
@Component
public final class Right2_OnArgSent extends InboundTransformer<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(Right2_OnArgSent.class);

    private final GlobalStateManager gsm;
    private final EventBus bus;

    @Inject
    public Right2_OnArgSent(final GlobalStateManager gsm,
                            final EventBus bus) {
        this.gsm = gsm;
        this.bus = bus;
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

        if (!ensureExactOneByte(ctx, msg))
            return fin.apply("bad data from line");

        final Bummer said = Bummer.find(msg.readByte());
        if (said != Bummer.OK)
            return fin.apply("line error: " + said);

        ctx.pipeline().remove(LengthFieldPrepender.class);
        ctx.pipeline().remove(this);
        gsm.setRightChannel(ctx.channel());

        log.info("line acquired");
        Rrr.info("line acquired");
        bus.post(Event.LINE_CONNECTED);

        return null;
    }

}
