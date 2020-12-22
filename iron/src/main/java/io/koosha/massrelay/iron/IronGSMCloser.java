package io.koosha.massrelay.iron;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

@ChannelHandler.Sharable
@Singleton
@Component
public final class IronGSMCloser extends ChannelInboundHandlerAdapter {

    private final IronGlobalStateManager gsm;

    @Inject
    public IronGSMCloser(final IronGlobalStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        gsm.kill();
        super.channelInactive(ctx);
    }

}
