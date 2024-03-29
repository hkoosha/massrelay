package io.koosha.massrelay.aluminum.base.nett;

import io.koosha.nettyfunctional.hook.InboundSink;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public abstract class CtxedHandler<T> extends InboundSink<T> {

    protected ChannelHandlerContext ctx;
    protected Channel thisChannel;

    @Override
    public final void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.thisChannel = ctx.channel();
    }

    @Override
    public final void handlerRemoved(final ChannelHandlerContext ctx) {
        this.ctx = null;
    }

    @Override
    public final void channelActive(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public final void channelInactive(final ChannelHandlerContext ctx) {
        this.ctx = null;
    }

}
