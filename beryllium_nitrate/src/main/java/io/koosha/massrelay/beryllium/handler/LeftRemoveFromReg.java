package io.koosha.massrelay.beryllium.handler;

import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.beryllium.svc.LeftChannelRegistry;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.doAllOf;

@ChannelHandler.Sharable
@Singleton
@Component
public final class LeftRemoveFromReg extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(LeftRemoveFromReg.class);

    private final LeftChannelRegistry registry;

    @Inject
    public LeftRemoveFromReg(final LeftChannelRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        doAllOf("LeftChannelRegistry", () -> {
            log.debug("removing left from registry on inactive: {}", ctx.channel().attr(Util.ID).get());
            registry.getAndRemove(ctx.channel().attr(Util.ID).get());
        });
    }

}
