package io.koosha.massrelay.copper.gui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.HostAndPort;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.func.Action;
import io.koosha.massrelay.aluminum.base.value.Result;
import io.koosha.massrelay.aluminum.base.value.Secret;
import io.koosha.massrelay.aluminum.base.value.Funcode;
import io.koosha.massrelay.aluminum.base.value.Funcodes;
import io.koosha.massrelay.copper.TunnelType;
import io.koosha.massrelay.copper.svc.Client;
import io.koosha.massrelay.copper.svc.Event;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import static io.koosha.massrelay.aluminum.base.Util.doAllOfE;

final class CommandCtrl {

    private static final Logger log = LoggerFactory.getLogger(CommandCtrl.class);

    private static final String P_LOGIN = "login";
    private static final String P_SECRET = "secret";
    private static final String P_REMOTE_DOMAIN = "remote";
    private static final String P_REMOTE_PORT = "remote_port";
    private static final String P_TCP_TARGET_DOMAIN = "tcp_target_domain";
    private static final String P_TCP_TARGET_PORT = "tcp_target_port";

    private final XGuiCtrl g;
    private final Preferences loginPref;
    private final Preferences settings;
    private final EventBus bus;

    CommandCtrl(XGuiCtrl g, EventBus bus) {
        this.g = g;
        this.bus = bus;
        this.loginPref = XGuiCtrl.injekt.getLoginPref();
        this.settings = XGuiCtrl.injekt.getSettings();

        Platform.runLater(((Action) () -> doAllOfE(
            "CommandCtrl::<init>",
            () -> g.tfLogin.setText(loginPref.get(P_LOGIN, "")),
            () -> g.tfPassword.setText(loginPref.get(P_SECRET, "")),
            () -> g.tfTargetDomain.setText(settings.get(P_TCP_TARGET_DOMAIN, "")),
            () -> g.tfTargetPort.setText(settings.get(P_TCP_TARGET_PORT, "")),
            () -> g.tfRemote.setText(settings.get(P_REMOTE_DOMAIN, "")),
            () -> g.tfRemotePort.setText(settings.get(P_REMOTE_PORT, ""))
        ))::exec);

        g.cmdTCP.setUserData(Funcodes.TCP);
        g.cmdCOM.setUserData(Funcodes.SERIAL);
        g.listenerTCP.setUserData(TunnelType.TCP);
        g.listenerCOM.setUserData(TunnelType.COM);

        bus.register(this);

        enable();
    }

    private static <T> T toggleData(final ToggleGroup tg) {
        @SuppressWarnings("unchecked")
        final T userData = (T) tg.getSelectedToggle().getUserData();
        return userData;
    }

    private static void disEn(final List<?> all, boolean disable) {
        for (Object object : all) {
            if (object instanceof ToggleGroup)
                for (Toggle toggle : ((ToggleGroup) object).getToggles())
                    ((RadioButton) toggle).setDisable(disable);
            else if (object instanceof TextField)
                ((TextField) object).setDisable(disable);
            else if (object instanceof Button)
                ((Button) object).setDisable(disable);
            else if (object instanceof AtomicBoolean)
                ((AtomicBoolean) object).set(!disable);
            else if (object instanceof ComboBox)
                ((ComboBox<?>) object).setDisable(disable);
            else if (object instanceof CheckBox)
                ((CheckBox) object).setDisable(disable);
            else
                throw new IllegalArgumentException("don't know how to disable/enable: " + object.getClass());
        }
    }

    private static Result<HostAndPort> parseHostAndPort(TextField tfDomain, TextField tfPort) {
        final String domain = tfDomain.getText();
        final String portText = tfPort.getText();

        final int port;
        try {
            port = Integer.parseInt(portText);
        }
        catch (NumberFormatException e) {
            return Result.fail("bad port");
        }

        try {
            return Result.ok(HostAndPort.fromParts(domain, port));
        }
        catch (IllegalArgumentException e) {
            return Result.fail("bad domain");
        }
    }

    @Subscribe
    void published(final Event event) {
        switch (event) {
            case RIGHT_CONNECTING -> {
                disable();
                updateServer("Dialing...");
            }
            case RIGHT_AUTHENTICATING -> {
                disable();
                updateServer("Saying hello...");
            }
            case RIGHT_CONNECTED -> {
                disable();
                updateServer("Online");
                updateLine("Waiting...");
            }
            case KILLED, RIGHT_DISCONNECTED -> {
                updateLine("Offline");
                updateServer("Offline");
                enable();
            }
            case LINE_CONNECTED -> updateLine("Online");
            case LINE_DISCONNECTED -> updateLine("Offline");
            case LEFT_CONNECTED -> updateLeft("Online");
            case LEFT_DISCONNECT -> updateLeft("Offline");
            default -> {
            }
            // To make spotbugs happy :|
        }
    }

    void cmdSelectTCP() {
        if (!XGuiCtrl.injekt.enabled())
            return;
        g.tgCommand.selectToggle(g.cmdTCP);
    }

    void cmdSelectCom() {
        if (!XGuiCtrl.injekt.enabled())
            return;
        g.tgCommand.selectToggle(g.cmdCOM);
    }

    void listenerSelectTCP() {
        if (!XGuiCtrl.injekt.enabled())
            return;
        g.tgListener.selectToggle(g.listenerTCP);
    }

    // ==================

    void listenerSelectCom() {
        if (!XGuiCtrl.injekt.enabled())
            return;
        g.tgListener.selectToggle(g.listenerCOM);
    }

    void connect() {
        if (!XGuiCtrl.injekt.enabled()) {
            Rrr.error("a connection is already in progress");
            return;
        }


        // =============== CLIENT

        final boolean clientOk = g.clientCtl.getSelectedClient() != null;
        if (clientOk) {
            final Client selected = g.clientCtl.getSelectedClient();
            updateEndPoint(selected.getId());
            XGuiCtrl.injekt.getGsm().setEndpoint(selected.getId());
        }
        else {
            Rrr.error("client is not set");
            updateEndPoint("?");
        }


        // =============== REMOTE

        final Result<HostAndPort> parseRemote = parseHostAndPort(g.tfRemote, g.tfRemotePort);
        if (parseRemote.isFailure()) {
            Rrr.error(parseRemote.getCause().getMessage());
            log.error(parseRemote.getCause().getMessage());
        }
        else {
            final HostAndPort remote = parseRemote.get();
            doAllOfE("CommandCtrl::connect() / REMOTE",
                () -> settings.put(P_REMOTE_DOMAIN, remote.getHost()),
                () -> settings.put(P_REMOTE_PORT, remote.getPort() > -1 ? remote.getPort() + "" : ""),
                settings::flush);
        }


        // =============== TCP FUNCODE TARGET

        final Result<HostAndPort> parseTarget = parseHostAndPort(g.tfTargetDomain, g.tfTargetPort);
        if (parseTarget.isSuccess()) {
            final HostAndPort target = parseTarget.get();
            doAllOfE("CommandCtrl::connect() / TARGET",
                () -> settings.put(P_TCP_TARGET_DOMAIN, target.getHost()),
                () -> settings.put(P_TCP_TARGET_PORT, target.getPort() > -1 ? target.getPort() + "" : ""),
                settings::flush);
            XGuiCtrl.injekt.getGsm().target(target);
        }


        // =============== LOGIN

        final String login = g.tfLogin.getText();
        final String secret = g.tfPassword.getText();
        doAllOfE("CommandCtrl::connect() / LOGIN",
            () -> loginPref.put(P_LOGIN, login),
            () -> loginPref.put(P_SECRET, secret),
            loginPref::flush);


        // =============== FUNCODE

        final Funcode cmd = toggleData(g.tgCommand);
        if (Objects.equals(cmd, Funcodes.SERIAL))
            XGuiCtrl.injekt.getGsm().funcode(Funcodes.SERIAL);
        else if (Objects.equals(cmd, Funcodes.TCP))
            XGuiCtrl.injekt.getGsm().funcode(Funcodes.TCP);
        else
            throw new IllegalStateException("" + cmd);


        // =============== LISTENER

        final TunnelType listener = toggleData(g.tgListener);
        if (Objects.equals(listener, TunnelType.COM))
            XGuiCtrl.injekt.getComService().enable();
        else if (Objects.equals(listener, TunnelType.TCP))
            XGuiCtrl.injekt.getComService().disable();
        else
            throw new IllegalStateException("" + listener);

        if (Objects.equals(cmd, Funcodes.TCP) && parseTarget.isFailure()) {
            Rrr.error(parseTarget.getCause().getMessage());
            log.error(parseTarget.getCause().getMessage());
            return;
        }
        else if (Objects.equals(listener, TunnelType.COM) && !g.comCtrl.setCom(false)) {
            return;
        }
        else if (parseRemote.isFailure() || !clientOk) {
            return;
        }

        disable();
        XGuiCtrl.injekt.getServerDialerService().dial(
            new Secret(login, secret),
            cmd, parseRemote.get()
        );
    }

    void kill(final String reason) {
        log.info("global kill switch, reason={}", reason);
        bus.post(Event.KILL);
    }

    private void updateLine(final String text) {
        Platform.runLater(((Action) () -> g.statusConnection.setText(text))::exec);
    }

    // ==================

    private void updateLeft(final String text) {
        Platform.runLater(((Action) () -> g.statusLocalSocket.setText(text))::exec);
    }

    private void updateServer(final String text) {
        Platform.runLater(((Action) () -> g.statusServer.setText(text))::exec);
    }

    private void updateEndPoint(final String text) {
        Platform.runLater(((Action) () -> g.statusEndPoint.setText(text))::exec);
    }

    private void disable() {
        Platform.runLater(((Action) () -> disEn(g.disableable(), true))::exec);
    }

    private void enable() {
        Platform.runLater(((Action) () -> disEn(g.disableable(), false))::exec);
    }

}
