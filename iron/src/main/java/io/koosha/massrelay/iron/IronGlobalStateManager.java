package io.koosha.massrelay.iron;

import com.google.common.net.HostAndPort;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;

import static io.koosha.nettyfunctional.NettyFunc.close;

@Singleton
@Component
public final class IronGlobalStateManager {

    private final Object LOCK = new Object();
    private Channel activeChannel = null;
    private HostAndPort hp = null;

    void setActiveChannel(final Channel v) {
        synchronized (LOCK) {
            this.activeChannel = v;
        }
    }

    public void kill() {
        synchronized (LOCK) {
            close(activeChannel);
            this.activeChannel = null;
            this.hp = null;
        }
    }

    public HostAndPort geRequest() {
        synchronized (LOCK) {
            return this.hp;
        }
    }

    public void setRequest(final HostAndPort hostAndPort) {
        synchronized (LOCK) {
            this.hp = hostAndPort;
        }
    }

}
