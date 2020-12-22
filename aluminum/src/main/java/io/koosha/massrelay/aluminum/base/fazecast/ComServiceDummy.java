package io.koosha.massrelay.aluminum.base.fazecast;

import io.koosha.massrelay.aluminum.base.Util;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ComServiceDummy implements ComService {

    private static final Logger log = LoggerFactory.getLogger(ComServiceDummy.class);

    private SerialKonfFazecast fazecast;

    @Override
    public boolean setPort(final SerialKonf serialKonf) {
        this.fazecast = serialKonf.toFazecast();
        log.info("com: {}", fazecast);
        return true;
    }

    @Override
    public void clearPort() {
        log.info("clearPort()");
    }

    @Override
    public void enable() {
        log.info("enable()");
    }

    @Override
    public void disable() {
        log.info("disable()");
    }

    @Override
    public void close() {
        log.info("close()");
    }

    @Override
    public boolean open() {
        log.info("open()");
        return true;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean makeWritable() {
        log.info("makeWritable()");
        return true;
    }

    @Override
    public boolean write(byte[] data) {
        log.info("discarding dummy serial data " + ByteBufUtil.prettyHexDump(
            Util.unpool(data)));
        return true;
    }

    @Override
    public SerialKonf getComKonf() {
        return fazecast == null ? null : fazecast.toSerialKonf();
    }

    @Override
    public void registerToData(ComDataListener listener) {
        log.info("registerToData() => {}", listener);
    }

}
