package io.koosha.massrelay.beryllium;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationFactory;
import io.koosha.massrelay.aluminum.Sslizer;
import io.koosha.massrelay.aluminum.base.file.FileService;
import io.koosha.massrelay.aluminum.base.nett.OnErrorCloser;
import io.koosha.massrelay.aluminum.base.qualify.Left;
import io.koosha.massrelay.aluminum.base.qualify.Right;
import io.koosha.massrelay.beryllium.handler.AccountingHandler;
import io.koosha.massrelay.beryllium.handler.AuthHandler;
import io.koosha.massrelay.beryllium.handler.LeftClientReqHandler;
import io.koosha.massrelay.beryllium.handler.LeftRemoveFromReg;
import io.koosha.massrelay.beryllium.handler.RightClientReqHandler;
import io.koosha.massrelay.beryllium.svc.ClientAuthenticationService;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static io.koosha.massrelay.aluminum.base.Util.init;

@Configuration
public class ZBerylliumProvider {

    @Singleton
    @Bean
    Konfiguration konfiguration(final FileService fs) {
        final String konfig = fs.read("classpath:berylliumNitrate.json");
        final List<Konfiguration> source = Collections.singletonList(
            KonfigurationFactory.getInstance().jacksonJson("json", konfig)
        );
        return KonfigurationFactory.getInstance().kombine("konfig", source);
    }

    // ---------------

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    ChannelTrafficShapingHandler tc(final Konfiguration k) {
        return new ChannelTrafficShapingHandler(
            k.long_("traffic.writeChannelLimit").v(0L),
            k.long_("traffic.readChannelLimit").v(0L),
            k.long_("traffic.checkInterval").v(1_000L),
            k.long_("traffic.waitInterval").v(2_000L)
        );
    }

    @Singleton
    @Bean
    AuthHandler leftAuthenticator(final ClientAuthenticationService as) {
        return new AuthHandler(as);
    }

    @Left
    @Singleton
    @Bean
    ChannelInitializer<Channel> leftIniter(final Sslizer sslizer,
                                           final Provider<WriteTimeoutHandler> wTimeout,
                                           final Provider<ReadTimeoutHandler> rTimeout,
                                           final Provider<ChannelTrafficShapingHandler> tc,
                                           final Provider<AccountingHandler> accounting,
                                           final Provider<LengthFieldBasedFrameDecoder> framed,
                                           final Provider<AuthHandler> auth,
                                           final Provider<LeftClientReqHandler> req,
                                           final Provider<LeftRemoveFromReg> reg,
                                           final Provider<OnErrorCloser> closer) {
        return init(ch -> {
            ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
            sslizer.server(ch).addLast(
                wTimeout.get(),
                rTimeout.get(),
                tc.get(),
                accounting.get(),
                framed.get(),
                auth.get(),
                req.get(),
                reg.get(),
                closer.get()
            );
            ch.read();
        });
    }

    @Right
    @Singleton
    @Bean
    ChannelInitializer<Channel> rightIniter(final Sslizer sslizer,
                                            final Provider<ReadTimeoutHandler> rTimeout,
                                            final Provider<WriteTimeoutHandler> wTimeout,
                                            final Provider<LengthFieldBasedFrameDecoder> framed,
                                            final Provider<AuthHandler> auth,
                                            final Provider<RightClientReqHandler> req,
                                            final Provider<ChannelTrafficShapingHandler> tc,
                                            final Provider<AccountingHandler> ac,
                                            final Provider<OnErrorCloser> closer) {
        return init(ch -> {
            ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
            sslizer.server(ch).addLast(
                wTimeout.get(),
                rTimeout.get(),
                tc.get(),
                ac.get(),
                framed.get(),
                auth.get(),
                req.get(),
                closer.get());
        });
    }

}
