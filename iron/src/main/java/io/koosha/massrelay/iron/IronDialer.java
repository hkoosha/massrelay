package io.koosha.massrelay.iron;

import com.google.common.net.HostAndPort;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.massrelay.aluminum.base.TaskGuard;
import io.koosha.massrelay.aluminum.base.func.CronTask;
import io.koosha.massrelay.aluminum.base.qualify.Payload;
import io.koosha.massrelay.aluminum.base.qualify.Right;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;

import static io.koosha.nettyfunctional.NettyFunc.writer;
import static io.koosha.massrelay.aluminum.base.Util.unpool;
import static io.koosha.massrelay.aluminum.base.Util.causeMsg;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public final class IronDialer extends CronTask {

    private static final Logger log = LoggerFactory.getLogger(IronDialer.class);

    private final Provider<byte[]> payload;
    private final HostAndPort remote;
    private final Provider<Bootstrap> bootstrap;
    private final TaskGuard taskGuard;
    private final boolean enabled;

    @Inject
    public IronDialer(@Payload final Provider<byte[]> payload,
                      @Right final HostAndPort remote,
                      @Right final Provider<Bootstrap> bootstrap,
                      final TaskGuard taskGuard,
                      final Konfiguration k) {
        this.payload = payload;
        this.remote = remote;
        this.bootstrap = bootstrap;
        this.taskGuard = taskGuard;
        this.enabled = k.bool("dialEnabled").v(true);
    }

    @Override
    protected void run() {
        if (!this.enabled) {
            log.trace("dialing skipped, not enabled");
            return;
        }

        if (taskGuard.isLoop()) {
            log.trace("dialing skipped, a connection is already in process");
            return;
        }

        log.info("dialing server -> {}", remote);
        taskGuard.start();

        final ByteBuf payload = unpool(this.payload.get());
        writer(bootstrap.get(), payload, t -> {
            log.warn("{}", causeMsg(t));
            if (ReferenceCountUtil.refCnt(payload) > 0)
                ReferenceCountUtil.release(payload);
            taskGuard.stop();
        });
    }

}
