package io.koosha.massrelay.copper;

import com.google.common.eventbus.EventBus;
import com.google.common.net.HostAndPort;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationFactory;
import io.koosha.massrelay.aluminum.Sslizer;
import io.koosha.massrelay.aluminum.base.fazecast.ComService;
import io.koosha.massrelay.aluminum.base.fazecast.ComServiceFazecast;
import io.koosha.massrelay.aluminum.base.file.FileService;
import io.koosha.massrelay.aluminum.base.qualify.Left;
import io.koosha.massrelay.aluminum.base.qualify.Right;
import io.koosha.massrelay.aluminum.base.qualify.Root;
import io.koosha.massrelay.copper.handler.LeftActiveChannelGuard;
import io.koosha.massrelay.copper.handler.LeftSocketHandler;
import io.koosha.massrelay.copper.handler.PayloadInjector;
import io.koosha.massrelay.copper.handler.Right0_OnAuthResponse;
import io.koosha.massrelay.copper.handler.Right1_OnLineAcquired;
import io.koosha.massrelay.copper.handler.Right3_ComHandler;
import io.koosha.massrelay.copper.handler.Right4_OnEnd;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import static io.koosha.massrelay.aluminum.base.Util.init;


@Configuration
public class ZCopperCarbonateProvider {

    @Singleton
    @Bean
    Konfiguration konfiguration(final FileService fs,
                                @Root final Preferences pref) {
        final String konfig = fs.read("classpath:copperCarbonate.json");
        final List<Konfiguration> source = Arrays.asList(
            KonfigurationFactory.getInstance().jacksonJson("json", konfig),
            KonfigurationFactory.getInstance().preferences("pref", pref)
        );
        return KonfigurationFactory.getInstance().kombine("konfig", source);
    }

    @Singleton
    @Bean
    @Root
    Preferences preferences() {
        return Preferences.userRoot().node("io.koosha.massrelay.konfiguration");
    }

    // ============================

    @Singleton
    @Bean
    EventBus bus() {
        return new EventBus();
    }

    // ============================

    @Lazy
    @Singleton
    @Bean
    ComService fazecast() {
        return new ComServiceFazecast();
    }

    // ============================

    @Bean
    @Singleton
    @Named(Names.CLIENTS)
    Preferences clients(@Root final Preferences preferences) {
        return preferences.node("clients");
    }

    @Bean
    @Singleton
    @Named(Names.SETTINGS)
    Preferences settings(@Root final Preferences preferences) {
        return preferences.node("settings");
    }

    @Bean
    @Named(Names.COM)
    Preferences com(@Root final Preferences preferences) {
        return preferences.node("com");
    }

    @Singleton
    @Bean
    @Named(Names.LOGIN)
    Preferences login(@Root final Preferences preferences) {
        return preferences.node("login");
    }


    // ============================

    @Right
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    Bootstrap internalBS(Bootstrap bootstrap,
                         @Right HostAndPort remote,
                         @Right ChannelInitializer<Channel> initer) {
        return bootstrap.clone()
                        .remoteAddress(remote.getHost(), remote.getPort())
                        .handler(initer);
    }

    @Left
    @Singleton
    @Bean
    ChannelInitializer<Channel> leftIniter(Provider<LeftActiveChannelGuard> guard,
                                           Provider<ReadTimeoutHandler> r,
                                           Provider<WriteTimeoutHandler> w,
                                           Provider<LeftSocketHandler> handler) {
        return init(ch -> {
            ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
            ch.pipeline().addLast(
                w.get(),
                r.get(),
                guard.get(),
                handler.get()
            ).read();
        });
    }

    @Right
    @Singleton
    @Bean
    ChannelInitializer<Channel> rightIniter(Sslizer sslizer,
                                            Provider<ReadTimeoutHandler> r,
                                            Provider<WriteTimeoutHandler> w,
                                            Provider<LengthFieldPrepender> framer0,
                                            Provider<PayloadInjector> inject,
                                            Provider<Right0_OnAuthResponse> auth,
                                            Provider<Right1_OnLineAcquired> line,
                                            Provider<Right3_ComHandler> com,
                                            Provider<Right4_OnEnd> end) {
        return init(ch -> {
            ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
            sslizer.client(ch).addLast(
                w.get(),
                r.get(),
                inject.get(),
                framer0.get(),
                auth.get(),
                Right1_OnLineAcquired.awaiter(),
                line.get(),
                com.get(),
                end.get()
            ).read();
        });
    }

}
