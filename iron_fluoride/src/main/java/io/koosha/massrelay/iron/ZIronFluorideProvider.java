package io.koosha.massrelay.iron;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationFactory;
import io.koosha.konfiguration.type.Kind;
import io.koosha.massrelay.aluminum.Sslizer;
import io.koosha.massrelay.aluminum.base.fazecast.ComService;
import io.koosha.massrelay.aluminum.base.fazecast.ComServiceFazecast;
import io.koosha.massrelay.aluminum.base.fazecast.SerialKonf;
import io.koosha.massrelay.aluminum.base.file.FileService;
import io.koosha.massrelay.aluminum.base.nett.EndLogger;
import io.koosha.massrelay.aluminum.base.nett.OnErrorCloser;
import io.koosha.massrelay.aluminum.base.nett.TaskGuardHandler;
import io.koosha.massrelay.aluminum.base.qualify.Payload;
import io.koosha.massrelay.aluminum.base.qualify.Right;
import io.koosha.massrelay.aluminum.base.value.Funcode;
import io.koosha.massrelay.aluminum.base.value.Funcodes;
import io.koosha.massrelay.aluminum.base.value.Secret;
import io.koosha.massrelay.iron.svc.DummySystemRestartService;
import io.koosha.massrelay.iron.svc.SystemRestartServiceBySudoCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static io.koosha.massrelay.aluminum.ZBaseProvider.PROFILE_DEV;
import static io.koosha.massrelay.aluminum.ZBaseProvider.PROFILE_PROD;
import static io.koosha.massrelay.aluminum.base.Util.encode;
import static io.koosha.massrelay.aluminum.base.Util.init;
import static io.koosha.massrelay.aluminum.base.Util.readBytes;
import static io.koosha.massrelay.aluminum.base.Util.unpool;
import static java.util.Collections.emptyList;

@Configuration
public class ZIronFluorideProvider {

    private static final Logger log = LoggerFactory.getLogger(ZIronFluorideProvider.class);

    private static final int VERSION = 0;

    @Singleton
    @Bean
    Konfiguration konfiguration(final FileService fs) {
        final String konfig = fs.read("classpath:ironFluoride.json");
        final List<Konfiguration> source = Collections.singletonList(
            KonfigurationFactory.getInstance().jacksonJson("json", konfig)
        );
        return KonfigurationFactory.getInstance().kombine("konfig", source);
    }

    @Profile(PROFILE_DEV)
    @Lazy
    @Singleton
    @Bean
    DummySystemRestartService dummySystemRestartService() {
        return new DummySystemRestartService();
    }

    @Profile(PROFILE_PROD)
    @Lazy
    @Singleton
    @Bean
    SystemRestartServiceBySudoCommand systemRestartServiceBySudoCommand() {
        return new SystemRestartServiceBySudoCommand();
    }


    @Singleton
    @Bean
    ComService comService(final Konfiguration k) {
        final SerialKonf sk = k.custom("serial", Kind.of(SerialKonf.class)).v();
        final List<String> disabled = k.list("disabledSerial", Kind.of(String.class)).v(emptyList());
        final ComServiceFazecast com = new ComServiceFazecast();

        if (ComServiceFazecast.hasStaticCom()) {
            if (!com.setPort(sk, ComServiceFazecast.getStaticCom()))
                log.error("=============== SERIAL NOT WRITABLE =============");
        }
        else {
            final SerialKonf goodSk = ComServiceFazecast.findAvailableCom(sk, disabled);
            if (goodSk == null) {
                log.warn("======= NO SERIAL PORT FOUND =======");
            }
            else {
                log.info("using port: {}", goodSk);
                if (!com.setPort(goodSk))
                    log.error("=============== SERIAL NOT WRITABLE =============");
            }
        }

        return com;
    }

    @Singleton
    @Bean
    Funcode funcode() {
        return Funcodes.SERIAL;
    }

    @Payload
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    byte[] payload(final Funcode funcode,
                   final Secret secret) {
        final ByteBuf u = unpool();
        u.writeInt(VERSION)
         .writeBytes(encode(secret.id(), secret.hash()))
         .writeByte(funcode.raw());
        return readBytes(u);
    }

    @Right
    @Singleton
    @Bean
    ChannelInitializer<Channel> channelInitializer(final Sslizer sslizer,
                                                   final Provider<ReadTimeoutHandler> rTimeout,
                                                   final Provider<WriteTimeoutHandler> wTimeout,
                                                   final Provider<EndLogger> endLogger,
                                                   final Provider<LengthFieldPrepender> framer,
                                                   final Provider<OnErrorCloser> onExClosePipe,
                                                   final Provider<IronCheckAuthResponse> auth,
                                                   final Provider<IronFluorideSerialRelay> relay,
                                                   final Provider<TaskGuardHandler> taskGuard) {
        return init(ch -> {
            ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
            sslizer.client(ch).addLast(
                wTimeout.get(),
                rTimeout.get(),
                endLogger.get(),
                framer.get(),
                taskGuard.get(),
                auth.get(),
                relay.get(),
                onExClosePipe.get()
            );
        });
    }

}
