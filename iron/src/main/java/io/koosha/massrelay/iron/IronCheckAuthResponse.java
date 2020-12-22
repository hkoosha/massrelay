package io.koosha.massrelay.iron;

import io.koosha.massrelay.aluminum.base.time.Now;
import io.koosha.massrelay.aluminum.base.value.Bummer;
import io.koosha.nettyfunctional.NettyFunc;
import io.koosha.nettyfunctional.hook.InboundSink;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.ensureExactOneByte;
import static io.koosha.massrelay.aluminum.base.Util.unpool;
import static io.koosha.nettyfunctional.NettyFunc.write;

@ChannelHandler.Sharable
@Singleton
@Component
public final class IronCheckAuthResponse extends InboundSink<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(IronCheckAuthResponse.class);

    private static final int VERSION = 0;

    private final TimeRemainingToRestartService at;
    private final IronGlobalStateManager gsm;
    private final Now now;

    @Inject
    public IronCheckAuthResponse(final TimeRemainingToRestartService at,
                                 final IronGlobalStateManager gsm,
                                 final Now now) {
        this.at = at;
        this.gsm = gsm;
        this.now = now;
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        if (!ensureExactOneByte(ctx, msg))
            return;

        final byte error = msg.readByte();
        final Bummer berylliumError = Bummer.find(error);
        if (Bummer.OK != berylliumError) {
            log.warn("EVENT :: authentication error: {}({})", berylliumError, error);
            NettyFunc.closeN(ctx);
            return;
        }

        ctx.pipeline().remove(LengthFieldPrepender.class);
        ctx.pipeline().remove(this);
        gsm.setActiveChannel(ctx.pipeline().channel());
        log.info("EVENT :: uplink");

        final ByteBuf payload = unpool()
            .writeInt(VERSION)
            .writeByte(Bummer.OK.raw())
            .writeLong(this.now.millis())
            .writeLong(at.millis());
        write(ctx, payload, ctx::read);
    }

}
