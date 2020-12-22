package io.koosha.massrelay.iron;

import com.google.common.net.HostAndPort;
import io.koosha.massrelay.aluminum.base.nett.ByteRelay;
import io.koosha.massrelay.aluminum.base.qualify.Left;
import io.koosha.massrelay.aluminum.base.value.Dump;
import io.koosha.nettyfunctional.hook.InboundSink;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.bg;
import static io.koosha.massrelay.aluminum.base.Util.unpool;
import static io.koosha.nettyfunctional.NettyFunc.connect;
import static io.koosha.nettyfunctional.NettyFunc.write;

@ChannelHandler.Sharable
@Singleton
@Component
public final class IronOxide3_ResponsePayloadHandler extends InboundSink<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(IronOxide3_ResponsePayloadHandler.class);

    private final IronGlobalStateManager gsm;
    private final Bootstrap bootstrap;
    private final Dump dump;

    @Inject
    public IronOxide3_ResponsePayloadHandler(final IronGlobalStateManager gsm,
                                             @Left final Bootstrap bootstrap,
                                             final Dump dump) {
        this.gsm = gsm;
        this.bootstrap = bootstrap;
        this.dump = dump;
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        final HostAndPort addr = gsm.geRequest();
        final ByteBuf later = unpool(msg);
        final Channel right = ctx.pipeline().channel();

        ctx.channel().config().setAutoRead(false);
        ctx.pipeline().remove(this);

        log.info("EVENT :: operation is a go, dialing target: {}", addr);
        bg(this.getClass().getSimpleName(),
            () -> connect(bootstrap.clone().remoteAddress(addr.getHost(), addr.getPort()),
                left -> {
                    log.info("making relays");

                    final ByteRelay rel0 = new ByteRelay(dump.right(), "right", left);
                    right.pipeline().addLast(rel0);

                    final ByteRelay rel1 = new ByteRelay(dump.left(), "left", right);
                    left.pipeline().addLast(rel1);

                    left.config().setAutoRead(false);
                    right.config().setAutoRead(false);

                    write(left, later, () -> {
                        left.read();
                        right.read();
                    });
                },
                throwable -> {
                    log.warn("target failed, killing origin without prior notice", throwable);
                    gsm.kill();
                    ctx.close();
                }));
    }

}
