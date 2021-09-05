package io.koosha.massrelay.aluminum.base.fazecast;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.fazecast.jSerialComm.SerialPort.EVEN_PARITY;
import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_CTS_ENABLED;
import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DISABLED;
import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DSR_ENABLED;
import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DTR_ENABLED;
import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_RTS_ENABLED;
import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED;
import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
import static com.fazecast.jSerialComm.SerialPort.MARK_PARITY;
import static com.fazecast.jSerialComm.SerialPort.NO_PARITY;
import static com.fazecast.jSerialComm.SerialPort.ODD_PARITY;
import static com.fazecast.jSerialComm.SerialPort.ONE_POINT_FIVE_STOP_BITS;
import static com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
import static com.fazecast.jSerialComm.SerialPort.SPACE_PARITY;
import static com.fazecast.jSerialComm.SerialPort.TWO_STOP_BITS;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public final class SerialKonfFazecast {

    private final String path;
    private final int stopBits;
    private final int parity;
    private final int baudRate;
    private final int dataBits;
    private final int timeout;
    private final int flowControl;
    private final boolean enabled;

    private final boolean dump;
    private final boolean stacktrace;
    private final int index;

    SerialKonfFazecast(final String path,
                       final int stopBits,
                       final int parity,
                       final int baudRate,
                       final int dataBits,
                       final int timeout,
                       final int flowControl,
                       final boolean enabled,
                       final boolean dump,
                       final boolean stacktrace,
                       final int index) {
        this.path = path;
        this.stopBits = stopBits;
        this.parity = parity;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.timeout = timeout;
        this.flowControl = flowControl;
        this.enabled = enabled;
        this.dump = dump;
        this.stacktrace = stacktrace;
        this.index = index;
    }

    public static SerialKonfFazecast valueOf(final SerialKonf serialKonf) {
        if (serialKonf.getDataBits() < 5 || serialKonf.getDataBits() > 9)
            throw new IllegalArgumentException("invalid data bits: " + serialKonf.getDataBits());

        if (serialKonf.getBaudRate() < 1)
            throw new IllegalArgumentException("invalid baud rate: " + serialKonf.getBaudRate());

        return new SerialKonfFazecast(serialKonf.getPath(),
                toFazecastStopBits(serialKonf.getStopBits()),
                toFazecastParity(serialKonf.getParity()),
                serialKonf.getBaudRate(),
                serialKonf.getDataBits(),
                serialKonf.getTimeout(),
                toFazecastFlowControl(serialKonf.getFlowControl()),
                serialKonf.isEnabled(),
                serialKonf.isDump(),
                serialKonf.isStacktrace(),
                serialKonf.getIndex());
    }

    public static SerialKonfFazecast valueOf(final SerialPort port) {
        return SerialKonfFazecast.builder()
                .path(port.getSystemPortName())
                .stopBits(port.getNumStopBits())
                .parity(port.getParity())
                .baudRate(port.getBaudRate())
                .dataBits(port.getNumDataBits())
                .timeout(port.getReadTimeout())
                .flowControl(port.getFlowControlSettings())
                .build();
    }

    public static String toSerialKonfParity(final int fazecastParity) {
        return switch (fazecastParity) {
            case NO_PARITY -> SerialKonf.PARITY_NONE;
            case EVEN_PARITY -> SerialKonf.PARITY_EVEN;
            case ODD_PARITY -> SerialKonf.PARITY_ODD;
            case MARK_PARITY -> SerialKonf.PARITY_MARK;
            case SPACE_PARITY -> SerialKonf.PARITY_SPACE;
            default -> throw new IllegalArgumentException("invalid fazecast parity: " + fazecastParity);
        };
    }

    public static String toSerialKonfStopBits(final int fazecastStopBits) {
        return switch (fazecastStopBits) {
            case ONE_POINT_FIVE_STOP_BITS -> SerialKonf.STOP_BITS_1_5;
            case ONE_STOP_BIT -> SerialKonf.STOP_BITS_1;
            case TWO_STOP_BITS -> SerialKonf.STOP_BITS_2;
            default -> throw new IllegalArgumentException("invalid fazecast stop bits: " + fazecastStopBits);
        };
    }

    public static Set<String> toSerialKonfFlowControl(final int fazecastFlowControl) {
        if (fazecastFlowControl == 0)
            return Collections.singleton(SerialKonf.FLOW_DISABLED);


        final Set<String> flowControl = new HashSet<>();

        if ((fazecastFlowControl & FLOW_CONTROL_RTS_ENABLED) != 0)
            flowControl.add(SerialKonf.FLOW_RTS);

        if ((fazecastFlowControl & FLOW_CONTROL_CTS_ENABLED) != 0)
            flowControl.add(SerialKonf.FLOW_CTS);

        if ((fazecastFlowControl & FLOW_CONTROL_DSR_ENABLED) != 0)
            flowControl.add(SerialKonf.FLOW_DSR);

        if ((fazecastFlowControl & FLOW_CONTROL_DTR_ENABLED) != 0)
            flowControl.add(SerialKonf.FLOW_DTR);

        if ((fazecastFlowControl & FLOW_CONTROL_XONXOFF_IN_ENABLED) != 0)
            flowControl.add(SerialKonf.FLOW_XONXOFF_IN);

        if ((fazecastFlowControl & FLOW_CONTROL_XONXOFF_OUT_ENABLED) != 0)
            flowControl.add(SerialKonf.FLOW_XONXOFF_OUT);

        return Collections.unmodifiableSet(flowControl);
    }

    public static int toFazecastParity(final String serialKonfParity) {
        return switch (serialKonfParity) {
            case SerialKonf.PARITY_NONE -> NO_PARITY;
            case SerialKonf.PARITY_ODD -> ODD_PARITY;
            case SerialKonf.PARITY_EVEN -> EVEN_PARITY;
            case SerialKonf.PARITY_MARK -> MARK_PARITY;
            case SerialKonf.PARITY_SPACE -> SPACE_PARITY;
            default -> throw new IllegalArgumentException("invalid SerialKonf parity: " + serialKonfParity);
        };
    }

    public static int toFazecastStopBits(final String serialKonfStopBits) {
        return switch (serialKonfStopBits) {
            case SerialKonf.STOP_BITS_1_5 -> ONE_POINT_FIVE_STOP_BITS;
            case SerialKonf.STOP_BITS_1 -> ONE_STOP_BIT;
            case SerialKonf.STOP_BITS_2 -> TWO_STOP_BITS;
            default -> throw new IllegalArgumentException("invalid SerialKonf stop bits: " + serialKonfStopBits);
        };
    }

    public static int toFazecastFlowControl(final Collection<String> serialKonfFlowControl) {
        if (serialKonfFlowControl.isEmpty() || serialKonfFlowControl.contains(SerialKonf.FLOW_DISABLED))
            return FLOW_CONTROL_DISABLED;

        int flowControl = FLOW_CONTROL_DISABLED;

        if (serialKonfFlowControl.contains(SerialKonf.FLOW_RTS))
            flowControl |= FLOW_CONTROL_RTS_ENABLED;

        if (serialKonfFlowControl.contains(SerialKonf.FLOW_CTS))
            flowControl |= FLOW_CONTROL_CTS_ENABLED;

        if (serialKonfFlowControl.contains(SerialKonf.FLOW_DSR))
            flowControl |= FLOW_CONTROL_DSR_ENABLED;

        if (serialKonfFlowControl.contains(SerialKonf.FLOW_DTR))
            flowControl |= FLOW_CONTROL_DTR_ENABLED;

        if (serialKonfFlowControl.contains(SerialKonf.FLOW_XONXOFF_IN))
            flowControl |= FLOW_CONTROL_XONXOFF_IN_ENABLED;

        if (serialKonfFlowControl.contains(SerialKonf.FLOW_XONXOFF_OUT))
            flowControl |= FLOW_CONTROL_XONXOFF_OUT_ENABLED;

        return flowControl;
    }

    public int getStopBitsAsInt() {
        return this.getStopBits();
    }

    public SerialKonf toSerialKonf() {
        return new SerialKonf(path,
                toSerialKonfStopBits(this.stopBits),
                toSerialKonfParity(this.parity),
                this.baudRate,
                this.dataBits,
                this.timeout,
                toSerialKonfFlowControl(this.flowControl),
                this.enabled,
                this.dump,
                this.stacktrace,
                this.index);
    }

    public String getPath() {
        return this.path;
    }

    public int getStopBits() {
        return this.stopBits;
    }

    public int getParity() {
        return this.parity;
    }

    public int getBaudRate() {
        return this.baudRate;
    }

    public int getDataBits() {
        return this.dataBits;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public int getFlowControl() {
        return this.flowControl;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isDump() {
        return this.dump;
    }

    public boolean isStacktrace() {
        return this.stacktrace;
    }

    public int getIndex() {
        return this.index;
    }

    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof final SerialKonfFazecast other))
            return false;
        if (!Objects.equals(this.getPath(), other.getPath()))
            return false;
        if (this.getStopBits() != other.getStopBits())
            return false;
        if (this.getParity() != other.getParity())
            return false;
        if (this.getBaudRate() != other.getBaudRate())
            return false;
        if (this.getDataBits() != other.getDataBits())
            return false;
        if (this.getTimeout() != other.getTimeout())
            return false;
        if (this.getFlowControl() != other.getFlowControl())
            return false;
        if (this.isEnabled() != other.isEnabled())
            return false;
        if (this.isDump() != other.isDump())
            return false;
        if (this.isStacktrace() != other.isStacktrace())
            return false;
        return this.getIndex() == other.getIndex();
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = PRIME + (this.getPath() == null ? 43 : this.getPath().hashCode());
        result = result * PRIME + this.getStopBits();
        result = result * PRIME + this.getParity();
        result = result * PRIME + this.getBaudRate();
        result = result * PRIME + this.getDataBits();
        result = result * PRIME + this.getTimeout();
        result = result * PRIME + this.getFlowControl();
        result = result * PRIME + (this.isEnabled() ? 79 : 97);
        result = result * PRIME + (this.isDump() ? 79 : 97);
        result = result * PRIME + (this.isStacktrace() ? 79 : 97);
        result = result * PRIME + this.getIndex();
        return result;
    }

    public String toString() {
        return "SerialKonfFazecast(path=" + this.getPath() +
                ", stopBits=" + this.getStopBits() +
                ", parity=" + this.getParity() +
                ", baudRate=" + this.getBaudRate() +
                ", dataBits=" + this.getDataBits() +
                ", timeout=" + this.getTimeout() +
                ", flowControl=" + this.getFlowControl() +
                ", enabled=" + this.isEnabled() +
                ", dump=" + this.isDump() +
                ", stacktrace=" + this.isStacktrace() +
                ", index=" + this.getIndex() + ")";
    }


    public static SerialKonfFazecastBuilder builder() {
        return new SerialKonfFazecastBuilder();
    }

    public static class SerialKonfFazecastBuilder {
        private String path;
        private int stopBits;
        private int parity;
        private int baudRate;
        private int dataBits;
        private int timeout;
        private int flowControl;
        private boolean enabled;
        private boolean dump;
        private boolean stacktrace;
        private int index;

        SerialKonfFazecastBuilder() {
        }

        public SerialKonfFazecastBuilder path(String path) {
            this.path = path;
            return this;
        }

        public SerialKonfFazecastBuilder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        public SerialKonfFazecastBuilder parity(int parity) {
            this.parity = parity;
            return this;
        }

        public SerialKonfFazecastBuilder baudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        public SerialKonfFazecastBuilder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        public SerialKonfFazecastBuilder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public SerialKonfFazecastBuilder flowControl(int flowControl) {
            this.flowControl = flowControl;
            return this;
        }

        public SerialKonfFazecastBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public SerialKonfFazecastBuilder dump(boolean dump) {
            this.dump = dump;
            return this;
        }

        public SerialKonfFazecastBuilder stacktrace(boolean stacktrace) {
            this.stacktrace = stacktrace;
            return this;
        }

        public SerialKonfFazecastBuilder index(int index) {
            this.index = index;
            return this;
        }

        public SerialKonfFazecast build() {
            return new SerialKonfFazecast(path, stopBits, parity, baudRate, dataBits,
                    timeout, flowControl, enabled, dump, stacktrace, index);
        }
    }

}
