package io.koosha.massrelay.copper.handler;

import com.google.common.eventbus.EventBus;
import io.koosha.nettyfunctional.NettyFunc;
import io.koosha.nettyfunctional.hook.InboundTransformer;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.value.Bummer;
import io.koosha.massrelay.copper.svc.Event;
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

import static io.koosha.massrelay.aluminum.base.Util.ensureAtLeastByte;


@ChannelHandler.Sharable
@Singleton
@Component
public final class Right0_OnAuthResponse extends InboundTransformer<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(Right0_OnAuthResponse.class);

    private final EventBus bus;

    @Inject
    public Right0_OnAuthResponse(final EventBus bus) {
        this.bus = bus;
    }

    @Override
    protected Object read1(final ChannelHandlerContext ctx,
                           final ByteBuf msg) {
        final Function<String, Void> fin = s -> {
            bus.post(Event.RIGHT_DISCONNECTED);
            bus.post(Event.LINE_DISCONNECTED);
            final Object[] args = new Object[]{};
            log.warn(s, args);
            Rrr.warn(s, args);
            NettyFunc.closeN(ctx);
            return null;
        };

        if (!ensureAtLeastByte(ctx, msg))
            return fin.apply("EVENT :: authenticating with server failed, bad data");

        switch (Bummer.find(msg.readByte())) {
            case AUTH_ERROR:
                return fin.apply("EVENT :: authentication failed");
            case ACCESS_DENIED:
                return fin.apply("EVENT :: access denied");
        }

        log.info("EVENT :: authenticated");
        bus.post(Event.RIGHT_CONNECTED);

        ctx.pipeline().remove(LengthFieldPrepender.class);
        ctx.pipeline().remove(this);
        return msg.readableBytes() > 0 ? msg.retain() : null;
    }

}
