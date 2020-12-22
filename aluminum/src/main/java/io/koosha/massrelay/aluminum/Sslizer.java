package io.koosha.massrelay.aluminum;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.massrelay.aluminum.base.file.FileService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public final class Sslizer {

    private static final Logger log = LoggerFactory.getLogger(Sslizer.class);

    private final SslContext client;
    private final SSLContext server;

    public Sslizer(final FileService fs,
                   final Konfiguration k) throws
        CertificateException,
        IOException,
        UnrecoverableKeyException,
        KeyManagementException,
        NoSuchAlgorithmException,
        KeyStoreException {
        final String konfigCrt = k.string("ssl.cert").v("");
        if (!konfigCrt.isEmpty()) {
            final X509Certificate cert = cert(fs.read(konfigCrt));
            this.client = clientize(cert);
            log.info("ssl enabled");
        }
        else {
            log.warn("CLIENT SSL NOT ENABLED: no certification was provided, connection is not secure!!");
            this.client = null;
        }

        final String p12 = k.string("ssl.p12").v("");
        final String pwd = k.string("ssl.pwd").v("");
        if (!p12.isEmpty()) {
            this.server = serverize(fs.read(p12), pwd);
        }
        else {
            this.server = null;
        }
    }

    private static X509Certificate cert(final String s) throws CertificateException {
        final InputStream is = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
    }

    private static SSLContext serverize(final String pkcs12Base64,
                                        final String pwd) throws
        KeyStoreException,
        CertificateException,
        NoSuchAlgorithmException,
        IOException,
        UnrecoverableKeyException,
        KeyManagementException {
        final KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(Base64.getDecoder().decode(pkcs12Base64)), pwd.toCharArray());

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, pwd.toCharArray());

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        log.info("server ssl enabled");
        return sslContext;
    }

    private static SslContext clientize(final X509Certificate cert) throws SSLException {
        return SslContextBuilder
            .forClient()
            .trustManager(cert)
            .build();
    }

    public ChannelPipeline client(final Channel channel) {
        if (client == null)
            return channel.pipeline();

        final SSLEngine engine = client.newEngine(channel.alloc());
        // engine.setNeedClientAuth(false);
        // engine.setUseClientMode(false);
        return channel.pipeline().addFirst(new SslHandler(engine));
    }

    public ChannelPipeline server(final Channel channel) {
        if (server == null)
            return channel.pipeline();

        final SSLEngine engine = server.createSSLEngine();
        engine.setNeedClientAuth(false);
        engine.setUseClientMode(false);
        return channel.pipeline().addFirst(new SslHandler(engine));
    }

}
