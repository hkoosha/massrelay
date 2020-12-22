package io.koosha.massrelay.copper.gui;

import com.fazecast.jSerialComm.SerialPort;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.fazecast.SerialKonf;
import io.koosha.massrelay.aluminum.base.func.Action;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static io.koosha.massrelay.aluminum.base.Util.cron;
import static io.koosha.massrelay.aluminum.base.Util.doAllOfE;
import static java.util.Collections.emptySet;

final class ComCtrl {

    private static final Logger log = LoggerFactory.getLogger(ComCtrl.class);

    private final static String P_PARITY = "parity";
    private final static String P_BAUD_RATE = "baud_rate";
    private final static String P_STOP_BITS = "stop_bits";
    private final static String P_DATA_BITS = "data_bits";

    private final ObservableList<String> portObs = FXCollections.observableArrayList();
    private final XGuiCtrl g;
    private final Preferences pref;

    ComCtrl(XGuiCtrl g, ScheduledExecutorService cron) {
        this.g = g;
        this.pref = XGuiCtrl.injekt.getComPref();
        init();
        cron(cron, 1500, ((Action) () -> Platform.runLater(((Action) this::reloadComNames)::exec))::exec);
    }

    private static List<String> getComNames() {
        return Arrays.stream(SerialPort.getCommPorts())
                     .map(SerialPort::getSystemPortName)
                     .sorted()
                     .collect(Collectors.toList());
    }

    private static boolean isInt(final TextField tf) {
        try {
            Integer.parseInt(tf.getText());
            return true;
        }
        catch (final NumberFormatException nfe) {
            return false;
        }
    }

    private static <T> T selection(final ComboBox<T> comboBox) {
        return comboBox.getSelectionModel().getSelectedItem();
    }

    private static boolean hasSelection(ComboBox<String> comboBox) {
        return comboBox.getSelectionModel() != null &&
            comboBox.getSelectionModel().getSelectedItem() != null &&
            !comboBox.getSelectionModel().getSelectedItem().isEmpty();
    }

    private void init() {
        reloadComNames();
        g.comCtlSelector.setItems(portObs);
        g.comCtlParity.setItems(FXCollections.observableArrayList(SerialKonf.PARITY));
        g.comCtlStopBits.setItems(FXCollections.observableArrayList(SerialKonf.STOP_BITS));
        g.comCtlDataBits.setItems(FXCollections.observableArrayList(SerialKonf.DATA_BITS));
        doAllOfE("ComCtrl::init()",
            pref::sync,
            () -> g.comCtlParity.getSelectionModel()
                                .select(pref.get(P_PARITY, "EVEN")),
            () -> g.comCtlStopBits.getSelectionModel()
                                  .select(pref.get(P_STOP_BITS, "1")),
            () -> g.comCtlDataBits.getSelectionModel()
                                  .select((Integer) pref.getInt(P_DATA_BITS, 7)),
            () -> g.tfComCtlBaudRate.setText(pref.getInt(P_BAUD_RATE, 9600) + ""),
            () -> g.comCtlSelector.getSelectionModel().select(0)
        );
        try {
            setCom(true);
        }
        catch (Exception npe) {
            log.error("could not load pref", npe);
        }

    }

    private void reloadComNames() {
        if (!XGuiCtrl.injekt.silenceEnabled())
            return;

        final List<String> newNames = getComNames();
        if (!portObs.containsAll(newNames)) {
            portObs.clear();
            portObs.addAll(newNames);
        }
    }

    boolean setCom(boolean init) {
        if (!XGuiCtrl.injekt.enabled())
            return false;

        killCom();

        if (!hasSelection(g.comCtlSelector)) {
            if (!init)
                Rrr.error("No com port selected");
            return false;
        }
        if (!getComNames().contains(selection(g.comCtlSelector))) {
            if (!init)
                Rrr.error("No such com port: " + selection(g.comCtlSelector));
            return false;
        }
        if (!isInt(g.tfComCtlBaudRate)) {
            if (!init)
                Rrr.error("Invalid baud rate");
            return false;
        }

        final int b = Integer.parseInt(g.tfComCtlBaudRate.getText());
        final String selPort = selection(g.comCtlSelector);
        final Integer dataBits = selection(g.comCtlDataBits);
        final String parity = selection(g.comCtlParity);
        final String stopBits = selection(g.comCtlStopBits);
        doAllOfE("ComCtrl::setCom()",
            () -> pref.put(P_PARITY, parity == null ? "EVEN" : parity),
            () -> pref.put(P_STOP_BITS, stopBits == null ? "1" : stopBits),
            () -> pref.putInt(P_DATA_BITS, dataBits == null
                ? 7
                : dataBits),
            () -> pref.putInt(P_BAUD_RATE, b),
            pref::flush
        );

        // Do not open com port upon initializing.
        if (init)
            return true;


        final SerialKonf sk = SerialKonf.builder()
                                        .path(selPort)
                                        .stopBits(stopBits)
                                        .parity(parity)
                                        .baudRate(b)
                                        .dataBits(dataBits)
                                        .timeout(10_000)
                                        .flowControl(emptySet())
                                        .enabled(true)
                                        .dump(false)
                                        .stacktrace(false)
                                        .index(0)
                                        .build();

        return XGuiCtrl.injekt.getComService().setPort(sk);
    }

    void killCom() {
        if (!XGuiCtrl.injekt.enabled())
            return;

        g.commandCtl.kill("kill com");
        XGuiCtrl.injekt.getComService().disable();
        ((Action) () -> Platform.runLater(((Action) () -> g.statusLocalCom.setText("Offline"))::exec)).exec();
    }

}
