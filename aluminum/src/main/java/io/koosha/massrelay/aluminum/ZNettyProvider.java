package io.koosha.massrelay.aluminum;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.massrelay.aluminum.base.file.FileService;
import io.koosha.massrelay.aluminum.base.nett.EndLogger;
import io.koosha.massrelay.aluminum.base.nett.OnErrorCloser;
import io.koosha.massrelay.aluminum.base.Util;
import io.koosha.massrelay.aluminum.base.value.Timeout;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.event.Level;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Component
public class ZNettyProvider {

    static final String MAX_PAYLOAD = "MAX_PAYLOAD";

    // TODO move to konfig
    @Singleton
    @Named(MAX_PAYLOAD)
    @Bean
    int maxPayload() {
        return 2 * 1024 * 1024;
    }

    @Singleton
    @Bean
    EndLogger endLogger() {
        return new EndLogger(Level.TRACE, Util::twoWayConnectionInfo);
    }

    @Singleton
    @Bean
    LoggingHandler loggingHandler() {
        return new LoggingHandler(LogLevel.TRACE);
    }

    @Singleton
    @Bean
    LengthFieldPrepender framer() {
        return new LengthFieldPrepender(4, false);
    }

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    LengthFieldBasedFrameDecoder framed(@Named(MAX_PAYLOAD) final int maxPayload) {
        return new LengthFieldBasedFrameDecoder(maxPayload,
            0,
            4,
            0,
            4);
    }

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    ReadTimeoutHandler readTimeoutHandler(final Timeout timeout) {
        return new ReadTimeoutHandler(timeout.getLeftRead(), TimeUnit.MILLISECONDS);
    }

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    WriteTimeoutHandler writeTimeoutHandler(final Timeout timeout) {
        return new WriteTimeoutHandler(timeout.getLeftWrite(), TimeUnit.MILLISECONDS);
    }

    @Singleton
    @Bean
    OnErrorCloser pureOnExCloser() {
        return new OnErrorCloser();
    }

    @Singleton
    @Bean
    EventLoopGroup looper() {
        return new NioEventLoopGroup();
    }

    // --------------

    @SuppressWarnings("TypeMayBeWeakened")
    @Singleton
    @Bean
    ServerBootstrap sb(final LoggingHandler loggingHandler) {
        return new ServerBootstrap()
            .channel(NioServerSocketChannel.class)
            .group(new NioEventLoopGroup())
            .handler(loggingHandler);
    }

    @Singleton
    @Bean
    Bootstrap bootstrapRaw(final EventLoopGroup looper) {
        return new Bootstrap()
            .channel(NioSocketChannel.class)
            .group(looper);
    }

    // --------------

    @Lazy
    @Singleton
    @Bean
    Sslizer sslizer(final FileService fs,
                    final Konfiguration k) throws Exception {
        return new Sslizer(fs, k);
    }

}
