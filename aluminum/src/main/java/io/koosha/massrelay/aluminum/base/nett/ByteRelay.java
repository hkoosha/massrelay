package io.koosha.massrelay.aluminum.base.nett;

import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.nettyfunctional.NettyFunc;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteRelay extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ByteRelay.class);

    protected final boolean dump;
    protected final String name;
    protected final Channel otherChannel;

    public ByteRelay(final boolean dump,
                     final String name,
                     final Channel otherChannel) {
        this.dump = dump;
        this.name = name;
        this.otherChannel = otherChannel;
    }

    @Override
    public final void channelRead(final ChannelHandlerContext ctx,
                                  final Object msg) {
        if (this.dump)
            log.info("{}:\n{}", name, ByteBufUtil.prettyHexDump((ByteBuf) msg));
        try {
            if (this.otherChannel.isOpen())
                this.otherChannel.writeAndFlush(Util.unpool((ByteBuf) msg))
                                 .addListener((it) -> {
                                     if (it.isSuccess()) {
                                         ctx.read();
                                     }
                                     else {
                                         log.warn("relay fail, closing: ", it.cause());
                                         markClose(ctx);
                                     }
                                 });
            else {
                this.markClose(ctx);
            }
        }
        catch (final Exception e) {
            ReferenceCountUtil.release(msg);
            this.markClose(ctx);
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        this.markClose(ctx);
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx,
                                final Throwable cause) {
        log.debug("relay closing on exception: {} :: {}", ctx.channel().id(), cause.getMessage());
        this.markClose(ctx);
        ctx.fireExceptionCaught(cause);
    }

    private void markClose(final ChannelHandlerContext ctx) {
        log.info("relay closing");
        Util.doAllOf("ByteRelay::_markClose()",
            () -> NettyFunc.close(ctx.channel()),
            () -> NettyFunc.close(otherChannel));
    }

}
