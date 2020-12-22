package io.koosha.massrelay.copper.handler;

import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.fazecast.ComDataListener;
import io.koosha.massrelay.aluminum.base.fazecast.ComService;
import io.koosha.massrelay.aluminum.base.nett.CtxedHandler;
import io.koosha.massrelay.aluminum.base.value.Dump;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.unpool;


@ChannelHandler.Sharable
@Singleton
@Component
public final class Right3_ComHandler extends CtxedHandler<ByteBuf> implements ComDataListener {

    private static final Logger log = LoggerFactory.getLogger(Right3_ComHandler.class);

    private final ComService comService;
    private final Dump dump;

    @Inject
    public Right3_ComHandler(final ComService comService,
                             final Dump dump) {
        this.comService = comService;
        this.dump = dump;
        comService.registerToData(this);
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        // Local com not enabled, requests are handled by network socket. pass
        // message to network socket handler.
        if (!comService.isEnabled()) {
            ctx.fireChannelRead(msg.retain());
            return;
        }

        if (dump.right()) {
            final String d = ByteBufUtil.prettyHexDump(msg);
            log.info("remote:\n{}", d);
            Rrr.info("remote:\n{}", d);
        }
        this.comService.write(Util.getBytes(msg));
    }

    @Override
    public void consume(final byte[] data) {
        if (dump.left()) {
            final String d = ByteBufUtil.prettyHexDump(unpool(data));
            log.info("local serial:\n{}", d);
            Rrr.info("local serial:\n{}", d);
        }
        if (!comService.isEnabled()) {
            log.warn("data from serial, but com is not enabled");
            Rrr.warn("data from serial, but com is not enabled");
            return;
        }
        if (this.ctx == null || !this.ctx.channel().isOpen()) {
            log.warn("no active remote channel. ignoring serial data");
            Rrr.warn("no active remote channel. ignoring serial data");
        }
        else {
            this.ctx.writeAndFlush(unpool(data));
        }
    }

}
