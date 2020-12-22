package io.koosha.massrelay.aluminum.base.nett;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.function.Function;

@ChannelHandler.Sharable
public final class EndLogger extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(EndLogger.class);

    private final Level atLevel;
    private final Function<Channel, Object> infoExtractor;

    public EndLogger(final Level atLevel,
                     final Function<Channel, Object> infoExtractor) {
        this.atLevel = atLevel;
        this.infoExtractor = infoExtractor;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        final String msg = "channel end: " + infoExtractor.apply(ctx.channel());
        switch (this.atLevel) {
            case ERROR:
                log.error(msg);
                break;
            case WARN:
                log.warn(msg);
                break;
            case INFO:
                log.info(msg);
                break;
            case DEBUG:
                log.debug(msg);
                break;
            case TRACE:
                log.trace(msg);
                break;
        }

        super.channelInactive(ctx);
    }

}
