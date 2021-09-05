package io.koosha.massrelay.aluminum.base.fazecast;

import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public final class SerialKonf {

    public static final String PARITY_NONE = "NONE";
    public static final String PARITY_ODD = "ODD";
    public static final String PARITY_EVEN = "EVEN";
    public static final String PARITY_MARK = "MARK";
    public static final String PARITY_SPACE = "SPACE";

    public static final int DATA_BITS_5 = 5;
    public static final int DATA_BITS_6 = 6;
    public static final int DATA_BITS_7 = 7;
    public static final int DATA_BITS_8 = 8;
    public static final int DATA_BITS_9 = 9;

    public static final int STOP_BITS_1_I = 1;
    public static final int STOP_BITS_2_I = 2;
    public static final String STOP_BITS_1 = "1";
    public static final String STOP_BITS_2 = "2";
    public static final String STOP_BITS_1_5 = "1.5";

    public static final String FLOW_DISABLED = "DISABLED";
    public static final String FLOW_RTS = "RTS";
    public static final String FLOW_CTS = "CTS";
    public static final String FLOW_DSR = "DSR";
    public static final String FLOW_DTR = "DTR";
    public static final String FLOW_XONXOFF_IN = "XONXOFF_IN";
    public static final String FLOW_XONXOFF_OUT = "XONXOFF_OUT";

    public static final Set<String> PARITY = Set.of(PARITY_NONE, PARITY_EVEN, PARITY_ODD, PARITY_MARK, PARITY_SPACE);

    public static final Set<String> STOP_BITS = Set.of("1.5", "1", "2");

    public static final Set<Integer> DATA_BITS = Set.of(DATA_BITS_5, DATA_BITS_6, DATA_BITS_7, DATA_BITS_8, DATA_BITS_9);

    public static final Set<String> FLOW_CONTROL = Set.of(FLOW_RTS, FLOW_CTS, FLOW_DSR, FLOW_DTR, FLOW_XONXOFF_IN, FLOW_XONXOFF_OUT);

    // -------------------------------------------------------------------------

    private final String path;
    private final String stopBits;
    private final String parity;
    private final int baudRate;
    private final int dataBits;
    private final int timeout;
    private final Set<String> flowControl;
    private final boolean enabled;
    private final boolean dump;
    private final boolean stacktrace;
    private final int index;

    SerialKonf(String path,
               String stopBits,
               String parity,
               int baudRate,
               int dataBits,
               int timeout,
               Set<String> flowControl,
               boolean enabled,
               boolean dump,
               boolean stacktrace,
               int index) {
        Objects.requireNonNull(flowControl);

        this.path = path;
        this.stopBits = stopBits;
        this.parity = parity;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.timeout = timeout;
        this.flowControl = Set.copyOf(flowControl);
        this.enabled = enabled;
        this.dump = dump;
        this.stacktrace = stacktrace;
        this.index = index;
    }


    public SerialKonfFazecast toFazecast() {
        return SerialKonfFazecast.valueOf(this);
    }

    public String getPath() {
        return this.path;
    }

    public String getStopBits() {
        return this.stopBits;
    }

    public String getParity() {
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

    public Set<String> getFlowControl() {
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

    public SerialKonf withPath(final String name) {
        return builder()
            .stopBits(this.stopBits)
            .parity(this.parity)
            .baudRate(this.baudRate)
            .dataBits(this.dataBits)
            .timeout(this.timeout)
            .flowControl(this.flowControl)
            .enabled(this.enabled)
            .dump(this.dump)
            .stacktrace(this.stacktrace)
            .index(this.index)
            .build();
    }


    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof final SerialKonf other))
            return false;
        if (!Objects.equals(this.getPath(), other.getPath()))
            return false;
        if (!Objects.equals(this.getStopBits(), other.getStopBits()))
            return false;
        if (!Objects.equals(this.getParity(), other.getParity()))
            return false;
        if (this.getBaudRate() != other.getBaudRate())
            return false;
        if (this.getDataBits() != other.getDataBits())
            return false;
        if (this.getTimeout() != other.getTimeout())
            return false;
        if (!Objects.equals(this.getFlowControl(), other.getFlowControl()))
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
        int result = 1;
        result = result * PRIME + (this.getPath() == null ? 43 : this.getPath().hashCode());
        result = result * PRIME + (this.getStopBits() == null ? 43 : this.getStopBits().hashCode());
        result = result * PRIME + (this.getParity() == null ? 43 : this.getParity().hashCode());
        result = result * PRIME + this.getBaudRate();
        result = result * PRIME + this.getDataBits();
        result = result * PRIME + this.getTimeout();
        result = result * PRIME + (this.getFlowControl() == null ? 43 : this.getFlowControl().hashCode());
        result = result * PRIME + (this.isEnabled() ? 79 : 97);
        result = result * PRIME + (this.isDump() ? 79 : 97);
        result = result * PRIME + (this.isStacktrace() ? 79 : 97);
        result = result * PRIME + this.getIndex();
        return result;
    }

    public String toString() {
        return "SerialKonf(path=" + this.getPath() +
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


    public static SerialKonfBuilder builder() {
        return new SerialKonfBuilder();
    }

    public static class SerialKonfBuilder {
        private String path;
        private String stopBits;
        private String parity;
        private int baudRate;
        private int dataBits;
        private int timeout;
        private Set<String> flowControl;
        private boolean enabled;
        private boolean dump;
        private boolean stacktrace;
        private int index;

        SerialKonfBuilder() {
        }

        public SerialKonfBuilder path(String path) {
            this.path = path;
            return this;
        }

        public SerialKonfBuilder stopBits(String stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        public SerialKonfBuilder parity(String parity) {
            this.parity = parity;
            return this;
        }

        public SerialKonfBuilder baudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        public SerialKonfBuilder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        public SerialKonfBuilder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public SerialKonfBuilder flowControl(Set<String> flowControl) {
            this.flowControl = flowControl;
            return this;
        }

        public SerialKonfBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public SerialKonfBuilder dump(boolean dump) {
            this.dump = dump;
            return this;
        }

        public SerialKonfBuilder stacktrace(boolean stacktrace) {
            this.stacktrace = stacktrace;
            return this;
        }

        public SerialKonfBuilder index(int index) {
            this.index = index;
            return this;
        }

        public SerialKonf build() {
            return new SerialKonf(path, stopBits, parity, baudRate, dataBits,
                timeout, flowControl, enabled, dump, stacktrace, index);
        }

        public String toString() {
            return "SerialKonf.SerialKonfBuilder(path=" + this.path +
                ", stopBits=" + this.stopBits +
                ", parity=" + this.parity +
                ", baudRate=" + this.baudRate +
                ", dataBits=" + this.dataBits +
                ", timeout=" + this.timeout +
                ", flowControl=" + this.flowControl +
                ", enabled=" + this.enabled +
                ", dump=" + this.dump +
                ", stacktrace=" + this.stacktrace +
                ", index=" + this.index +
                ")";
        }
    }

}
