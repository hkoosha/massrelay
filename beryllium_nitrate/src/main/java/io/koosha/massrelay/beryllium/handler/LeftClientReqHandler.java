package io.koosha.massrelay.beryllium.handler;

import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.aluminum.base.time.Now;
import io.koosha.massrelay.aluminum.base.value.Bummer;
import io.koosha.massrelay.aluminum.base.value.Funcode;
import io.koosha.massrelay.aluminum.base.value.Funcodes;
import io.koosha.massrelay.beryllium.entity.Client;
import io.koosha.massrelay.beryllium.entity.ClientRepo;
import io.koosha.massrelay.beryllium.entity.HLeftClientRequestHistory;
import io.koosha.massrelay.beryllium.entity.HLeftClientRequestHistoryRepo;
import io.koosha.massrelay.beryllium.entity.PermissionEvent;
import io.koosha.massrelay.beryllium.svc.LeftChannelRegistry;
import io.koosha.massrelay.beryllium.svc.LeftToRightAccessService;
import io.koosha.nettyfunctional.hook.InboundSink;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.bg;
import static io.koosha.massrelay.aluminum.base.Util.bgOr;
import static io.koosha.massrelay.aluminum.base.Util.ensureAtLeastByte;
import static io.koosha.massrelay.aluminum.base.Util.ensureBytesRange;
import static io.koosha.nettyfunctional.NettyFunc.close;
import static io.koosha.nettyfunctional.NettyFunc.write;

@ChannelHandler.Sharable
@Singleton
@Component
public final class LeftClientReqHandler extends InboundSink<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(LeftClientReqHandler.class);

    private final HLeftClientRequestHistoryRepo hitRepo;
    private final ClientRepo cr;
    private final LeftToRightAccessService access;
    private final LeftChannelRegistry registry;
    private final Now now;

    @Inject
    public LeftClientReqHandler(final HLeftClientRequestHistoryRepo hitRepo,
                                final ClientRepo cr,
                                final LeftToRightAccessService access,
                                final LeftChannelRegistry registry,
                                final Now now) {
        this.hitRepo = hitRepo;
        this.cr = cr;
        this.access = access;
        this.registry = registry;
        this.now = now;
    }

    @Override
    protected void read2(final ChannelHandlerContext ctx,
                         final ByteBuf msg) {
        final long now = this.now.millis();
        final String lid = ctx.channel().attr(Util.ID).get();
        final String remote = Util.socketAddrInfo(ctx.channel().remoteAddress());

        if (!ensureAtLeastByte(ctx, msg)) {
            close(ctx);
            return;
        }

        final byte rf = msg.readByte();
        final Funcode funcode;
        if (rf == Funcodes.TCP.raw())
            funcode = Funcodes.TCP;
        else if (rf == Funcodes.SERIAL.raw())
            funcode = Funcodes.SERIAL;
        else
            funcode = Funcode.UNKNOWN;

        if (!ensureBytesRange(ctx, msg, 1, 128)) {
            close(ctx);
            return;
        }

        final String rid = Util.read(msg);

        bgOr("LeftClientReqHandler::read2()",
            () -> {
                // TODO what about concurrent mod in db? do not throw, return err.
                // Found already by Auth handler.
                final Client left = cr.findById(lid).orElse(null);
                if (left == null)
                    throw new IllegalStateException("left client not found: " + lid);

                final Client right = cr.findById(rid).orElse(null);
                if (right == null) {
                    log.warn("EVENT :: access denied, right client not found: {} id={} remote={}",
                        rid, left.getId(), remote);
                    close(ctx, Util.unpool(Bummer.ACCESS_DENIED.raw()));
                    return;
                }
                if (!right.getEnabled()) {
                    log.warn("EVENT :: access denied right client not enabled: {} id={} remote={}",
                        right.getId(), left.getId(), remote);
                    close(ctx, Util.unpool(Bummer.ACCESS_DENIED.raw()));
                    return;
                }

                if (access.check(left, right, funcode) != PermissionEvent.GRANT) {
                    log.warn("EVENT :: access denied: {} => {} funcode={} remote={}",
                        left.getId(), right.getId(), funcode, remote);
                    close(ctx, Util.unpool(Bummer.ACCESS_DENIED.raw()));
                    return;
                }

                bg("LeftClientReqHandler::exec()", () ->
                    hitRepo.tSave(HLeftClientRequestHistory.start(left, now)));
                ctx.pipeline().remove(this);

                // TODO race condition, order of: left connection, right connection, left ack receive
                // Probably won't matter? right won't act till we add left to registry.
                close(registry.getAndRemove(left.getId()));
                write(ctx, Util.unpool(Bummer.OK.raw()), () -> {
                    log.info("said ok to left client: {} => {} funcode={} remote={}",
                        left.getId(), right.getId(), funcode, remote);
                    registry.put(ctx.channel(), right.getId(), funcode);
                });
            },
            er -> {
                log.warn("err", er);
                ctx.close();
            });
    }

}
