package io.koosha.massrelay.copper;

import com.google.common.net.HostAndPort;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.nett.NettyServing;
import io.koosha.massrelay.aluminum.base.qualify.Left;
import io.koosha.massrelay.aluminum.base.qualify.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

import static io.koosha.massrelay.aluminum.base.Util.bg;


@Singleton
@Component
public final class CopperCarbonateAppRunner {

    private static final Logger log = LoggerFactory.getLogger(CopperCarbonateAppRunner.class);
    private final ServerBootstrap sb;
    private final ExecutorService serverRunner;
    private final HostAndPort addr;
    private final ChannelInitializer<? extends Channel> init;

    private NettyServing ns;

    @Inject
    public CopperCarbonateAppRunner(ServerBootstrap sb,
                                    @Server ExecutorService serverRunner,
                                    @Left HostAndPort addr,
                                    @Left ChannelInitializer<? extends Channel> init) {
        this.sb = sb;
        this.serverRunner = serverRunner;
        this.addr = addr;
        this.init = init;
    }

    public void run() {
        bg(this.getClass().getSimpleName(), () -> {
            final ServerBootstrap serve = sb.clone().childHandler(init);
            ns = new NettyServing(serve, addr);
            serverRunner.submit(ns);
            log.info("running local server on: {}", addr);
            Rrr.info("running local server on: " + addr);
        });
    }

    public void stop() {
        log.info("stopping local server");
        ns.fin();
    }

}
