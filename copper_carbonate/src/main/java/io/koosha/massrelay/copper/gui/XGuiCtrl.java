package io.koosha.massrelay.copper.gui;

import io.koosha.massrelay.copper.svc.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class XGuiCtrl {

    private static final Logger log = LoggerFactory.getLogger(XGuiCtrl.class);

    static XInjekt injekt;

    @FXML
    TextArea err0;

    // _________________________________________________________________ CONNECT

    @FXML
    Button btnConnect;
    @FXML
    TextField tfTargetPort;
    @FXML
    TextField tfTargetDomain;
    @FXML
    TextField tfLogin;
    @FXML
    PasswordField tfPassword;

    // _________________________________________________________ CLIENTS COLUMNS

    @FXML
    TableView<Client> tblClients;
    //    @FXML
    //    TableColumn<Client, String> tblClientModId;
    @FXML
    TextField tfNewClientId;

    // _____________________________________________________________________ CMD

    @FXML
    ToggleGroup tgCommand;
    @FXML
    RadioButton cmdCOM;
    @FXML
    RadioButton cmdTCP;

    @FXML
    ToggleGroup tgListener;
    @FXML
    RadioButton listenerCOM;
    @FXML
    RadioButton listenerTCP;

    @FXML
    TextField tfRemote;
    @FXML
    TextField tfRemotePort;

    // __________________________________________________________________ STATUS

    @FXML
    Label statusEndPoint;
    @FXML
    Label statusConnection;
    @FXML
    Label statusServer;
    @FXML
    Label statusLocalCom;
    @FXML
    Label statusLocalSocket;

    // ____________________________________________________________________ MENU

    @FXML
    MenuItem menuAbout;
    @FXML
    MenuItem menuExit;
    @FXML
    MenuItem menuConnect;
    @FXML
    MenuItem menuDisconnect;
    @FXML
    MenuItem menuCmdCOM;
    @FXML
    MenuItem menuCmdTCP;
    @FXML
    MenuItem menuListenerCOM;
    @FXML
    MenuItem menuListenerTCP;

    // _____________________________________________________________________ COM

    @FXML
    ComboBox<String> comCtlSelector;
    @FXML
    ComboBox<String> comCtlParity;
    @FXML
    ComboBox<String> comCtlStopBits;
    @FXML
    ComboBox<Integer> comCtlDataBits;
    @FXML
    TextField tfComCtlBaudRate;
    @FXML
    Button btnKillCom;

    // ________________________________________________________________________

    ComCtrl comCtrl;
    ClientCtrl clientCtl;
    MenuCtl menuCtl;
    CommandCtrl commandCtl;
    ErrorCtrl errCtrl;

    void customInit() {
        this.clientCtl = new ClientCtrl(this);
        this.commandCtl = new CommandCtrl(this, injekt.getBus());
        this.comCtrl = new ComCtrl(this, XGuiCtrl.injekt.getCron());
        this.menuCtl = new MenuCtl(this);
        this.errCtrl = new ErrorCtrl(this);
    }

    // ________________________________________________________________________

    @FXML
    void onConnect(ActionEvent event) {
        commandCtl.connect();
    }

    @FXML
    void onKill(ActionEvent event) {
        commandCtl.kill("disconnect button");
    }

    @FXML
    void onKillCom(ActionEvent event) {
        comCtrl.killCom();
    }

    // ______________________________________________________________

    List<Object> disableable() {
        return Arrays.asList(tgCommand,
            tgListener,
            tfTargetPort,
            tfTargetDomain,

            btnConnect,
            tfRemote,
            tfRemotePort,
            tfLogin,
            tfPassword,

            comCtlSelector,
            comCtlParity,
            comCtlStopBits,
            comCtlDataBits,
            tfComCtlBaudRate,
            btnKillCom,

            tfNewClientId,

            injekt.enabled
        );

    }

}
