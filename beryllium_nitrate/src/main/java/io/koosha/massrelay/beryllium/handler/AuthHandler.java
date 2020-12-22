package io.koosha.massrelay.beryllium.handler;

import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.aluminum.base.value.Bummer;
import io.koosha.massrelay.aluminum.base.value.Result;
import io.koosha.massrelay.aluminum.base.value.Secret;
import io.koosha.massrelay.beryllium.svc.AuthenticationService;
import io.koosha.nettyfunctional.hook.InboundSink;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.koosha.massrelay.aluminum.base.Util.ID;
import static io.koosha.massrelay.aluminum.base.Util.bgOr;
import static io.koosha.nettyfunctional.NettyFunc.close;

@ChannelHandler.Sharable
public final class AuthHandler extends InboundSink<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(AuthHandler.class);

    private static final int VERSION = 0;

    private final AuthenticationService authService;

    public AuthHandler(final AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        final String info = Util.twoWayConnectionInfo(ctx.channel());
        final String remote = Util.socketAddrInfo(ctx.channel().remoteAddress());

        // TODO wait, what? is it pre-handled by lengthBasedFieldBlaBla?
        if (msg.readableBytes() < 4) {
            log.warn("corrupt data, from={}", info);
            ctx.close();
            return;
        }

        final int version = msg.readInt();
        if (version != VERSION) {
            log.warn("unsupported version from={} version={}", info, version);
            ctx.close();
            return;
        }

        final Result<Secret> s = decodeSecret(msg);
        if (s.isFailure()) {
            log.warn("could not decode secret, from={}", info);
            return;
        }
        final Secret secret = s.get();

        log.trace("connection={} for={}", info, secret.getId());

        msg.retain();
        bgOr("AuthHandler",
            () -> {
                if (!authService.isAuthenticated(secret.getId(), secret.getHash())) {
                    msg.release();
                    log.warn("EVENT :: wrong authentication id={} remote={}", secret.getId(), remote);
                    final ByteBuf write = Util.unpool(Bummer.AUTH_ERROR.raw());
                    close(ctx, write);
                    return;
                }

                log.debug("EVENT :: authenticated id={} remote={}", secret.getId(), remote);

                ctx.channel().attr(ID).set(secret.getId());

                ctx.pipeline().remove(this);
                ctx.pipeline().remove(LengthFieldBasedFrameDecoder.class);

                if (msg.readableBytes() > 0)
                    ctx.fireChannelRead(msg);
                else
                    msg.release();
            },
            er -> {
                log.warn("{}, {}, bg task failed", info, er);
                msg.release();
                ctx.close();
            });
    }


    private static Result<Secret> decodeSecret(final ByteBuf b) {
        final Result<List<String>> bDecode = Util.decodeStrings(b, 2);
        if (bDecode.isFailure())
            return Result.fail(bDecode.getCause());
        final List<String> sDecode = bDecode.get();
        return Result.ok(new Secret(sDecode.get(0), sDecode.get(1)));
    }

}
