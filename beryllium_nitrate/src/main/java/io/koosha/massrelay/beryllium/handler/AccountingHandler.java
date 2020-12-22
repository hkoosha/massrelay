package io.koosha.massrelay.beryllium.handler;

import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.aluminum.base.time.Now;
import io.koosha.massrelay.beryllium.entity.Client;
import io.koosha.massrelay.beryllium.entity.ClientRepo;
import io.koosha.massrelay.beryllium.entity.HAccounting;
import io.koosha.massrelay.beryllium.entity.HAccountingRepo;
import io.koosha.massrelay.beryllium.entity.HLeftClientRequestHistory;
import io.koosha.massrelay.beryllium.entity.HLeftClientRequestHistoryRepo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.bg;
import static io.koosha.massrelay.aluminum.base.Util.twoWayConnectionInfo;

@ChannelHandler.Sharable
@Singleton
@Component
public final class AccountingHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(AccountingHandler.class);

    private final ClientRepo cr;
    private final HLeftClientRequestHistoryRepo hitRepo;
    private final HAccountingRepo accR;
    private final Now now;

    @Inject
    public AccountingHandler(final ClientRepo cr,
                             final HLeftClientRequestHistoryRepo hitRepo,
                             final HAccountingRepo accR,
                             final Now now) {
        this.cr = cr;
        this.hitRepo = hitRepo;
        this.accR = accR;
        this.now = now;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        try {
            super.channelInactive(ctx);
        }
        finally {
            final ChannelTrafficShapingHandler counter = ctx
                .pipeline()
                .get(ChannelTrafficShapingHandler.class);

            final String id = ctx.channel().attr(Util.ID).get();
            if (id == null)
                log.warn("channel has no id: {}", twoWayConnectionInfo(ctx.channel()));
            else
                bg("AccountingHandler", () -> {
                    final Client client = cr.findById(id).orElse(null);
                    if (client == null) {
                        log.warn("channel has no client, remote={}",
                            Util.socketAddrInfo(ctx.channel().remoteAddress()));
                        return;
                    }

                    if (counter == null) {
                        log.warn("channel has no counter: {}", id);
                    }
                    else {
                        log.trace("accounting {}", id);
                        final TrafficCounter tc = counter.trafficCounter();
                        final HAccounting ac = HAccounting.create(
                            client, tc.cumulativeWrittenBytes(), tc.cumulativeReadBytes());
                        accR.tSave(ac);
                    }

                    hitRepo.tSave(HLeftClientRequestHistory.end(client, this.now.millis()));
                });
        }
    }

}
