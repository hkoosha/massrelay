package io.koosha.massrelay.beryllium;

import com.google.common.net.HostAndPort;
import io.koosha.massrelay.aluminum.base.nett.NettyServing;
import io.koosha.massrelay.aluminum.base.qualify.Left;
import io.koosha.massrelay.aluminum.base.qualify.Right;
import io.koosha.massrelay.aluminum.base.qualify.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;

import static io.koosha.massrelay.aluminum.base.Util.bg;
import static io.koosha.massrelay.aluminum.base.Util.doAllOf;

@Component
public final class BerylliumAppRunner {

    private static final Logger log = LoggerFactory.getLogger(BerylliumAppRunner.class);

    private final ServerBootstrap sb;
    private final ExecutorService serverRunner;

    private final ChannelInitializer<Channel> leftChannelIniter;
    private final ChannelInitializer<Channel> rightChannelIniter;

    private final HostAndPort leftAddr;
    private final HostAndPort rightAddr;

    private NettyServing nsLeft;
    private NettyServing nsRight;

    @Inject
    public BerylliumAppRunner(final ServerBootstrap sb,
                              @Server final ExecutorService serverRunner,
                              @Left final ChannelInitializer<Channel> leftChannelIniter,
                              @Right final ChannelInitializer<Channel> rightChannelIniter,
                              @Left final HostAndPort leftAddr,
                              @Right final HostAndPort rightAddr) {
        this.sb = sb;
        this.serverRunner = serverRunner;

        this.leftAddr = leftAddr;
        this.rightAddr = rightAddr;

        this.leftChannelIniter = leftChannelIniter;
        this.rightChannelIniter = rightChannelIniter;
    }

    public void run() {
        bg("BerylliumAppRunner", this::leftServer);
        bg("BerylliumAppRunner", this::rightServer);
    }

    private void leftServer() {
        final ServerBootstrap s = sb.clone().childHandler(leftChannelIniter);
        this.nsLeft = new NettyServing(s, leftAddr);
        this.serverRunner.submit(nsLeft);
        log.info("running left server on: {}", leftAddr);
    }

    private void rightServer() {
        final ServerBootstrap s = sb.clone().childHandler(rightChannelIniter);
        this.nsRight = new NettyServing(s, rightAddr);
        this.serverRunner.submit(nsRight);
        log.info("running right server on: {}", rightAddr);
    }

    public void stop() {
        doAllOf("BerylliumAppRunner::stop()",
            () -> {
                log.info("stopping left server");
                this.nsLeft.fin();
            },
            () -> {
                log.info("stopping right server");
                this.nsRight.fin();
            }
        );
    }

}
