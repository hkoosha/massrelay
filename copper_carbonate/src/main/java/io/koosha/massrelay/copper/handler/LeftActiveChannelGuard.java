package io.koosha.massrelay.copper.handler;

import com.google.common.eventbus.EventBus;
import io.koosha.nettyfunctional.NettyFunc;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.fazecast.ComService;
import io.koosha.massrelay.copper.svc.Event;
import io.koosha.massrelay.copper.svc.GlobalStateManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.causeMsg;
import static io.koosha.massrelay.aluminum.base.Util.twoWayConnectionInfo;


@ChannelHandler.Sharable
@Singleton
@Component
public final class LeftActiveChannelGuard extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(LeftActiveChannelGuard.class);

    private final EventBus eventBus;
    private final GlobalStateManager gsm;
    private final ComService comService;

    @Inject
    public LeftActiveChannelGuard(final EventBus eventBus,
                                  final GlobalStateManager gsm,
                                  final ComService comService) {
        this.eventBus = eventBus;
        this.gsm = gsm;
        this.comService = comService;
    }

    private boolean check(final ChannelHandlerContext ctx) {
        if (comService.isEnabled()) {
            final Object[] args = new Object[]{};
            log.warn("left com service is enabled, skipping network client", args);
            Rrr.warn("left com service is enabled, skipping network client", args);
            NettyFunc.close(ctx);
            return false;
        }

        if (gsm.hasLeftChannel()) {
            final Object[] args = new Object[]{twoWayConnectionInfo(ctx.channel())};
            log.warn("already has a left channel. skipping: {}", args);
            Rrr.warn("already has a left channel. skipping: {}", args);
            NettyFunc.close(ctx);
            return false;
        }

        if (!gsm.hasViableRightChannel()) {
            final Object[] args = new Object[]{twoWayConnectionInfo(ctx.channel())};
            log.warn("no active right channel. skipping: {}", args);
            Rrr.warn("no active right channel. skipping: {}", args);
            NettyFunc.close(ctx);
            return false;
        }

        return true;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (check(ctx)) {
            gsm.setLeftChannel(ctx.channel());
            eventBus.post(Event.LEFT_CONNECTED);
            super.channelActive(ctx);
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        log.trace("closing on pipeline end");
        eventBus.post(Event.LEFT_DISCONNECT);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx,
                                final Throwable cause) {
        log.warn("closing on pipeline exception: {}", causeMsg(cause));
        ctx.close();
    }

}
