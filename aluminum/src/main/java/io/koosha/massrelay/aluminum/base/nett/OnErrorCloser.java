package io.koosha.massrelay.aluminum.base.nett;

import io.koosha.massrelay.aluminum.base.Util;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public final class OnErrorCloser extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(OnErrorCloser.class);

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx,
                                final Throwable cause) {
        log.error("close / pipeline exception: {}", Util.causeMsg(cause));
        ctx.close();
    }

}
