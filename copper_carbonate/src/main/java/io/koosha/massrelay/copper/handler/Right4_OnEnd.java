package io.koosha.massrelay.copper.handler;

import com.google.common.eventbus.EventBus;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.copper.svc.Event;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.causeMsg;

@ChannelHandler.Sharable
@Singleton
@Component
public final class Right4_OnEnd extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(Right4_OnEnd.class);

    private final EventBus bus;

    @Inject
    public Right4_OnEnd(final EventBus bus) {
        this.bus = bus;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        log.trace("closing on pipeline end");
        clear();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx,
                                final Throwable cause) {
        log.warn("closing on pipeline exception: {}", causeMsg(cause));
        Rrr.warn("{}", causeMsg(cause));
        ctx.close();
        clear();
    }

    private void clear() {
        bus.post(Event.LINE_DISCONNECTED);
        bus.post(Event.RIGHT_DISCONNECTED);
        log.info("right disconnected");
    }

}
