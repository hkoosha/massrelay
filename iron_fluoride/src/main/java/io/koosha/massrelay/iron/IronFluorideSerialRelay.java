package io.koosha.massrelay.iron;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.aluminum.base.fazecast.ComDataListener;
import io.koosha.massrelay.aluminum.base.fazecast.ComService;
import io.koosha.massrelay.aluminum.base.nett.CtxedHandler;
import io.koosha.massrelay.aluminum.base.value.Dump;
import io.koosha.massrelay.iron.svc.SystemRestartService;
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
import static io.koosha.nettyfunctional.NettyFunc.writer;

@ChannelHandler.Sharable
@Singleton
@Component
public final class IronFluorideSerialRelay extends CtxedHandler<ByteBuf> implements ComDataListener {

    private static final Logger log = LoggerFactory.getLogger(IronFluorideSerialRelay.class);

    private static final String RESTART = "restartOnSerialFailure";

    private final SystemRestartService srs;
    private final ComService comService;
    private final Dump dump;
    private final boolean restart;

    private final Object LOCK = new Object();

    @Inject
    public IronFluorideSerialRelay(final SystemRestartService srs,
                                   final ComService comService,
                                   final Dump dump,
                                   final Konfiguration konfiguration) {
        this.srs = srs;
        this.comService = comService;
        this.dump = dump;
        this.restart = konfiguration.bool(RESTART).v(false);
        comService.registerToData(this);
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        if (this.dump.right())
            log.info("ToSerial:\n{}", ByteBufUtil.prettyHexDump(msg));

        if (!this.comService.makeWritable()) {
            log.error("EVENT :: SERIAL IS NOT WRITABLE!");
            ctx.close();
            if (restart) {
                log.warn("EVENT :: restarting on serial failure...");
                srs.exec();
            }
        }

        log.trace("writing into: {}", this.comService);
        synchronized (LOCK) {
            this.comService.write(Util.readBytes(msg));
        }
        ctx.read();
    }

    @Override
    public void consume(final byte[] data) {
        synchronized (LOCK) {
            if (this.thisChannel == null || !this.thisChannel.isOpen()) {
                log.warn("serial data while there is no viable channel");
                return;
            }
            final ByteBuf unpooled = unpool(data);
            if (this.dump.left())
                log.info("FromSerial:\n{}", ByteBufUtil.prettyHexDump(unpooled));
            writer(this.thisChannel, unpooled, t -> log.warn("could not write serial data into channel", t));
        }
    }

}
