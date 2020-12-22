package io.koosha.massrelay.beryllium.handler;

import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.aluminum.base.nett.ByteRelay;
import io.koosha.massrelay.aluminum.base.value.Bummer;
import io.koosha.massrelay.aluminum.base.value.Dump;
import io.koosha.massrelay.aluminum.base.value.Funcode;
import io.koosha.massrelay.beryllium.svc.LeftChannelRegistry;
import io.koosha.nettyfunctional.hook.InboundSink;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.bgOr;
import static io.koosha.massrelay.aluminum.base.Util.ensureExactOneByte;
import static io.koosha.nettyfunctional.NettyFunc.close;
import static io.koosha.nettyfunctional.NettyFunc.write;

@ChannelHandler.Sharable
@Singleton
@Component
public final class RightClientReqHandler extends InboundSink<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(RightClientReqHandler.class);

    private final LeftChannelRegistry lcr;
    private final Dump dump;

    @Inject
    public RightClientReqHandler(final LeftChannelRegistry lcr,
                                 final Dump dump) {
        this.lcr = lcr;
        this.dump = dump;
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        if (!ensureExactOneByte(ctx, msg))
            return;

        final Funcode funcode = Funcode.find(msg.readByte());
        final String remote = Util.socketAddrInfo(ctx.channel().remoteAddress());
        final String rid = ctx.channel().attr(Util.ID).get();

        bgOr("RightClientReqHandler",
            () -> {
                final Channel other = lcr.getAndRemove(rid, funcode);
                if (other == null || !other.isOpen()) {
                    log.debug("no left, id={}", rid);
                    log.trace("EVENT :: not expecting rightId={} funcode={} remote={}",
                        rid, funcode, remote);
                    close(ctx);
                    return;
                }

                ctx.channel().config().setAutoRead(false);
                other.config().setAutoRead(false);

                final ByteRelay rel0 = new ByteRelay(dump.left(), "left", ctx.channel());
                other.pipeline().addLast(rel0);

                final ByteRelay rel1 = new ByteRelay(dump.right(), "right", other);
                ctx.channel().pipeline().replace(this, null, rel1);

                write(ctx, Util.unpool(Bummer.OK.raw()), () -> {
                    other.read();
                    ctx.channel().read();
                }, throwable -> other.close());
            },
            er -> {
                log.error("err", er);
                ctx.close();
            }
        );
    }

}
