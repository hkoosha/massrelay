package io.koosha.massrelay.copper.handler;

import io.koosha.nettyfunctional.hook.InboundSink;
import io.koosha.massrelay.aluminum.base.nett.ByteRelay;
import io.koosha.massrelay.aluminum.base.value.Dump;
import io.koosha.massrelay.copper.svc.GlobalStateManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

@ChannelHandler.Sharable
@Singleton
@Component
public final class LeftSocketHandler extends InboundSink<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(LeftSocketHandler.class);

    private final GlobalStateManager gsm;
    private final Dump dump;

    @Inject
    public LeftSocketHandler(final GlobalStateManager gsm,
                             final Dump dump) {
        this.gsm = gsm;
        this.dump = dump;
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        log.info("relaying local tcp connection");

        final Channel right = gsm.getRightChannel();
        final Channel left = ctx.channel();

        right.config().setAutoRead(false);
        left.config().setAutoRead(false);

        if (left.pipeline().names().contains("relay"))
            left.pipeline().remove("relay");
        if (right.pipeline().names().contains("relay"))
            right.pipeline().remove("relay");

        left.pipeline().replace(
            this,
            "relay",
            new ByteRelay(dump.left(), "left", right) {
                @Override
                public void channelInactive(ChannelHandlerContext ctx1) {
                    ctx1.fireChannelInactive();
                }
            }
        );

        right.pipeline().addLast(
            "relay",
            new ByteRelay(dump.right(), "right", left)
        );

        right.pipeline().get(PayloadInjector.class).inject(msg.retain());

        left.read();
        right.read();
    }

}
