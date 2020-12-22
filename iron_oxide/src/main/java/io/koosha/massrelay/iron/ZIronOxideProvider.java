package io.koosha.massrelay.iron;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationFactory;
import io.koosha.massrelay.aluminum.Sslizer;
import io.koosha.massrelay.aluminum.base.file.FileService;
import io.koosha.massrelay.aluminum.base.nett.EndLogger;
import io.koosha.massrelay.aluminum.base.nett.OnErrorCloser;
import io.koosha.massrelay.aluminum.base.nett.TaskGuardHandler;
import io.koosha.massrelay.aluminum.base.qualify.Left;
import io.koosha.massrelay.aluminum.base.qualify.Payload;
import io.koosha.massrelay.aluminum.base.qualify.Right;
import io.koosha.massrelay.aluminum.base.value.Funcode;
import io.koosha.massrelay.aluminum.base.value.Funcodes;
import io.koosha.massrelay.aluminum.base.value.Secret;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static io.koosha.massrelay.aluminum.base.Util.encode;
import static io.koosha.massrelay.aluminum.base.Util.init;
import static io.koosha.massrelay.aluminum.base.Util.readBytes;
import static io.koosha.massrelay.aluminum.base.Util.unpool;

@Configuration
public class ZIronOxideProvider {

    private static final int VERSION = 0;

    @Singleton
    @Bean
    Konfiguration konfiguration(final FileService fs) {
        final String konfig = fs.read("classpath:ironOxide.json");
        final List<Konfiguration> source = Collections.singletonList(
            KonfigurationFactory.getInstance().jacksonJson("json", konfig)
        );
        return KonfigurationFactory.getInstance().kombine("konfig", source);
    }

    @Singleton
    @Bean
    Funcode funcode() {
        return Funcodes.TCP;
    }

    @Payload
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    byte[] payload(final Funcode funcode,
                   final Secret secret) {
        final ByteBuf u = unpool();
        u.writeInt(VERSION).writeBytes(encode(secret.id(), secret.hash())).writeByte(funcode.raw());
        return readBytes(u);
    }

    @Left
    @Singleton
    @Bean
    Bootstrap localBootstrap(@Left final ChannelInitializer<Channel> initer,
                             final EventLoopGroup looper) {
        return new Bootstrap()
            .channel(NioSocketChannel.class)
            .group(looper)
            .handler(initer);
    }

    @Right
    @Singleton
    @Bean
    ChannelInitializer<Channel> rightChannelInitializer(final Sslizer sslizer,
                                                        final Provider<ReadTimeoutHandler> rTimeout,
                                                        final Provider<WriteTimeoutHandler> wTimeout,
                                                        final Provider<LengthFieldPrepender> framer,
                                                        final Provider<OnErrorCloser> onExClosePipe,
                                                        final Provider<TaskGuardHandler> taskGuard,
                                                        final Provider<EndLogger> end,
                                                        final Provider<IronCheckAuthResponse> auth,
                                                        final Provider<LengthFieldBasedFrameDecoder> framed,
                                                        final Provider<IronOxide2_ResponseAddrHandler> addr,
                                                        final Provider<IronOxide3_ResponsePayloadHandler> payload) {
        return init(ch -> {
            ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
            sslizer.client(ch).addLast(
                rTimeout.get(),
                wTimeout.get(),
                framer.get(),
                taskGuard.get(),
                auth.get(),
                framed.get(),
                addr.get(),
                payload.get(),
                end.get(),
                onExClosePipe.get()
            );
        });
    }

    @Left
    @Singleton
    @Bean
    ChannelInitializer<Channel> leftChannelInitializer(final Provider<ReadTimeoutHandler> rTimeout,
                                                       final Provider<WriteTimeoutHandler> wTimeout,
                                                       final Provider<OnErrorCloser> onExClosePipe,
                                                       final Provider<EndLogger> end) {
        return init(ch -> {
            ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
            ch.config().setAutoRead(false);
            ch.pipeline().addLast(
                wTimeout.get(),
                rTimeout.get(),
                onExClosePipe.get(),
                end.get()
            );
        });
    }


}
