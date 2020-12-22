package io.koosha.massrelay.aluminum.base.nett;

import com.google.common.net.HostAndPort;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class NettyServing implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(NettyServing.class);

    private final AbstractBootstrap<?, ?> bootstrap;
    private final ChannelFuture bindFuture;

    public NettyServing(final AbstractBootstrap<?, ?> bootstrap,
                        final HostAndPort on) {
        this.bootstrap = bootstrap;
        this.bindFuture = bootstrap.bind(on.getHost(), on.getPort());
    }

    @Override
    public void run() {
        try {
            this.tryRun();
        }
        catch (final Exception e) {
            log.error("tryRun failed", e);
        }
    }

    private void tryRun() throws InterruptedException {
        this.bindFuture.sync().channel().closeFuture().sync();
        this.bootstrap.config().group().shutdownGracefully().sync();
    }

    public void fin() {
        this.bootstrap
            .config()
            .group()
            .shutdownGracefully(10, 1000, TimeUnit.MILLISECONDS);
    }

}
