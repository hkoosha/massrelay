package io.koosha.massrelay.iron;

import com.google.common.net.HostAndPort;
import io.koosha.massrelay.aluminum.base.TaskGuard;
import io.koosha.massrelay.aluminum.base.nett.TaskGuardHandler;
import io.koosha.massrelay.aluminum.base.qualify.Internal;
import io.koosha.massrelay.aluminum.base.qualify.Right;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.inject.Singleton;

@Configuration
public class ZIronBaseProvider {

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    TaskGuardHandler taskGuardHandler(final TaskGuard tg) {
        return new TaskGuardHandler(tg);
    }

    @Right
    @Internal
    @Singleton
    @Bean
    Bootstrap internalBS(final Bootstrap bootstrapRaw,
                         @Right final HostAndPort remote,
                         @Right final ChannelInitializer<Channel> initer) {
        return bootstrapRaw.clone()
                           .remoteAddress(remote.getHost(), remote.getPort())
                           .handler(initer);
    }

    @Right
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    Bootstrap bootstrap(@Right @Internal final Bootstrap bootstrapBs) {
        return bootstrapBs.clone();
    }

}
