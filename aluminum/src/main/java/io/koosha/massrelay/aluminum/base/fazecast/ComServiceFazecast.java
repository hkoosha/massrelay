package io.koosha.massrelay.aluminum.base.fazecast;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static io.koosha.massrelay.aluminum.base.Util.causeMsg;

public final class ComServiceFazecast implements ComService {

    private static final Logger log = LoggerFactory.getLogger(ComServiceFazecast.class);

    private static SerialPort staticCom = null;
    private final boolean stacktrace;
    private final List<ComDataListener> comDataListeners = new CopyOnWriteArrayList<>();
    private volatile boolean enabled = false;
    private volatile SerialPort com;


    // -------------------
    private volatile SerialKonfFazecast fazecast;

    public ComServiceFazecast() {
        this.stacktrace = true;
    }


    public ComServiceFazecast(final SerialPort serialPort,
                              final boolean stacktrace) {
        this.stacktrace = stacktrace;
        this.setPort(SerialKonfFazecast.valueOf(serialPort).toSerialKonf(), serialPort);
    }


    public static boolean hasStaticCom() {
        return staticCom != null;
    }

    public static SerialKonf findAvailableCom(final SerialKonf sk,
                                              final List<String> disabled) {
        final SerialPort[] ports = SerialPort.getCommPorts();

        final ArrayList<String> totalDis = new ArrayList<>();
        final ArrayList<String> totalFail = new ArrayList<>();

        for (int i = 0; i < ports.length; i++) {
            final SerialPort port = ports[i];
            final String name = port.getSystemPortName();
            if (!name.contains("AMA") && !name.contains("USB"))
                continue;

            boolean skip = false;
            for (final String d : disabled)
                if (name.endsWith(d)) {
                    skip = true;
                    totalDis.add(name);
                    break;
                }

            boolean fail = false;
            if (!skip)
                try {
                    if (!port.openPort(100))
                        throw new Exception("can not open port: " + name);
                }
                catch (Exception e) {
                    fail = true;
                    totalFail.add(name);
                }
                finally {
                    try {
                        port.closePort();
                    }
                    catch (Exception ignore) {

                    }
                }

            if (!fail && !skip) {
                log.info("found index={} total={} name={}",
                    i, ports.length, name);
                return sk.withPath(name);
            }
        }

        log.warn("no serial port found, disabled={}, failed={}", totalDis, totalFail);
        return null;
    }

    public static String getDamnName(final SerialPort sp) throws Exception {
        final Field field = SerialPort.class.getDeclaredField("comPort");
        field.setAccessible(true);
        return (String) field.get(sp);
    }

    public static SerialPort getStaticCom() {
        return ComServiceFazecast.staticCom;
    }

    public static void setStaticCom(final SerialPort staticCom) {
        ComServiceFazecast.staticCom = staticCom;
    }

    private static SerialPort find(final SerialKonf serialKonf) {
        for (final SerialPort sp : SerialPort.getCommPorts())
            if (serialKonf.getPath().endsWith(sp.getSystemPortName()))
                return sp;

        log.error("no such com port: {}", serialKonf.getPath());
        return null;
    }

    private static SerialPortDataListener listen(final Consumer<byte[]> listener) {
        return new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                final SerialPort com = event.getSerialPort();

                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    log.warn("unrequested serial mode: {}", event.getEventType());
                    return;
                }
                else if (com.bytesAvailable() < 1) {
                    sleep();
                    log.warn("zero data event: {}", com.bytesAvailable());
                    return;
                }

                final byte[] buffer = new byte[com.bytesAvailable()];
                final int read = com.readBytes(buffer, buffer.length);
                if (read != buffer.length)
                    log.error("no all data was read, read={} expected={}", read, buffer.length);
                else
                    listener.accept(buffer);
            }
        };
    }

    private static void sleep() {
        try {
            Thread.sleep(100);
        }
        catch (final InterruptedException e) {
            log.warn("sleep interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private boolean error(final String msg) {
        log.error(msg);
        sleep();
        if (stacktrace)
            log.debug("error", new RuntimeException("stacktrace"));
        return false;
    }

    private boolean event(final byte[] data) {
        if (!isEnabled())
            return error("com data read, while disabled, discarding");
        if (this.comDataListeners.isEmpty())
            return error("serial data but no listeners registered");
        for (final ComDataListener e : this.comDataListeners)
            e.consume(this.comDataListeners.size() == 1 ? data : Arrays.copyOf(data, data.length));
        return true;
    }

    // _________________________________________________________________________

    @Override
    public void enable() {
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void close() {
        final SerialPort serial = this.com;
        if (serial == null) {
            return;
        }
        try {
            try {
                serial.removeDataListener();
            }
            finally {
                if (!serial.closePort())
                    log.error("could not close port");
            }
        }
        catch (final Throwable e) {
            log.error("err closing serial port", e);
        }
    }

    // _________________________________________________________________________

    @Override
    public boolean open() {
        if (this.com == null)
            return error("com port not set");
        else if (this.com.isOpen())
            return true;
        else
            try {
                return this.com.openPort() || error("can not open port");
            }
            catch (Exception e) {
                return error("can not open port: " + causeMsg(e));
            }
    }

    @Override
    public boolean isOpen() {
        return this.com != null && this.com.isOpen();
    }

    @Override
    public boolean makeWritable() {
        if (!this.isEnabled())
            this.enable();

        if (this.com == null)
            return error("com port not set");
        else if (!this.com.isOpen())
            return this.open();
        else
            return true;
    }

    @Override
    public SerialKonf getComKonf() {
        return fazecast == null ? null : fazecast.toSerialKonf();
    }

    // _________________________________________________________________________

    @Override
    public void clearPort() {
        if (this.com != null)
            this.close();
        this.com = null;
    }

    @Override
    public boolean setPort(final SerialKonf serialKonf) {
        return setPort(serialKonf, find(serialKonf));
    }

    public boolean setPort(SerialKonf serialKonf,
                           final SerialPort serialPort) {
        if (serialPort == null)
            return error("com port not found: " + serialKonf);
        serialKonf = serialKonf.withPath(serialPort.getSystemPortName());

        log.info("setting new com: {} / {}", serialKonf, serialPort.getSystemPortName());
        log.info("found com: {}", serialPort.getSystemPortName());

        this.clearPort();
        this.com = serialPort;
        try {
            if (!this.makeWritable())
                return false;
            log.info("clearing com listeners");
            this.com.removeDataListener();
            log.info("registering root serial listener");
            if (!com.addDataListener(listen(this::event)))
                return error("could not add serial listener");
        }
        catch (Exception e) {
            this.clearPort();
            return error("could not open com port: " + causeMsg(e));
        }

        final String name = this.com == null ? "NO_COM_PORT" : this.com.getSystemPortName();
        log.info("new com port: {}", name);

        this.fazecast = serialKonf.toFazecast();
        com.setComPortParameters(this.fazecast.getBaudRate(),
            this.fazecast.getDataBits(),
            this.fazecast.getStopBits(),
            this.fazecast.getParity());
        com.setFlowControl(this.fazecast.getFlowControl());
        log.info("new com config: childListeners={} com={}", this.comDataListeners.size(), this.fazecast);
        this.enable();
        return this.makeWritable();
    }

    public boolean write(final byte[] data) {
        if (!this.isEnabled())
            return error("com data while com is not enabled");
        else if (this.com == null)
            return error("no com available to write to");

        try {
            this.com.writeBytes(data, data.length);
            return true;
        }
        catch (final Exception e) {
            return error("local serial write failed: " + causeMsg(e));
        }
    }

    @Override
    public void registerToData(final ComDataListener listener) {
        log.info("registering listener: {}", listener);
        this.comDataListeners.add(listener);
    }

}
