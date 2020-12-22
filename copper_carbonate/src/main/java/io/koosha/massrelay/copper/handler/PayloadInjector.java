package io.koosha.massrelay.copper.handler;

import io.koosha.massrelay.aluminum.base.nett.CtxedHandler;
import io.koosha.nettyfunctional.NettyFunc;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

@ChannelHandler.Sharable
@Singleton
@Component
public final class PayloadInjector extends CtxedHandler<ByteBuf> {

    @Inject
    public PayloadInjector() {
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        ctx.fireChannelRead(msg.retain());
    }

    void inject(final Object object) {
        NettyFunc.write(this.ctx, object);
    }

}
