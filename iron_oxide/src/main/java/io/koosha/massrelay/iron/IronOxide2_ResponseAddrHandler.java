package io.koosha.massrelay.iron;

import com.google.common.net.HostAndPort;
import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.aluminum.base.value.Bummer;
import io.koosha.massrelay.aluminum.base.value.Result;
import io.koosha.nettyfunctional.NettyFunc;
import io.koosha.nettyfunctional.hook.InboundSink;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static io.koosha.nettyfunctional.NettyFunc.write;

@ChannelHandler.Sharable
@Singleton
@Component
public final class IronOxide2_ResponseAddrHandler extends InboundSink<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(IronOxide2_ResponseAddrHandler.class);

    private final IronGlobalStateManager gsm;

    @Inject
    public IronOxide2_ResponseAddrHandler(final IronGlobalStateManager gsm) {
        this.gsm = gsm;
    }

    private static Result<HostAndPort> decodeHostAndPort(final ByteBuf b) {
        final Result<List<String>> bDecode = Util.decodeStrings(b, 1);
        if (bDecode.isFailure())
            return Result.fail(bDecode.getCause());
        if (b.readableBytes() < 1)
            return Result.fail();
        final List<String> sDecode = bDecode.get();
        final int port = b.readInt();
        final HostAndPort h = port > -1
            ? HostAndPort.fromParts(sDecode.get(0), port)
            : HostAndPort.fromHost(sDecode.get(0));
        return Result.ok(h);
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        final Result<HostAndPort> hp = decodeHostAndPort(msg);
        if (hp.isFailure()) {
            gsm.kill();
            log.warn("EVENT :: could not decode host and port from: {}", Util.socketAddrInfo(ctx.channel().remoteAddress()));
            NettyFunc.closeN(ctx);
            return;
        }
        else if (msg.readableBytes() > 0) {
            gsm.kill();
            log.warn("EVENT :: garbage data from {}", Util.socketAddrInfo(ctx.channel().remoteAddress()));
            NettyFunc.closeN(ctx);
            return;
        }
        else {
            gsm.setRequest(hp.get());
        }

        ctx.pipeline().remove(this);
        ctx.pipeline().remove(LengthFieldBasedFrameDecoder.class);

        log.info("EVENT :: request from origin: {}", hp);
        write(ctx, Util.unpool(Bummer.OK.raw()));
    }

}
