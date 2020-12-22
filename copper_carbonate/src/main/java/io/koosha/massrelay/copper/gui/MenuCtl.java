package io.koosha.massrelay.copper.gui;

import io.koosha.massrelay.CopperCarbonateApplication;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


final class MenuCtl {

    private static final Logger log = LoggerFactory.getLogger(MenuCtl.class);

    MenuCtl(final XGuiCtrl g) {
        g.menuExit.setOnAction(event -> exit());
        g.menuAbout.setOnAction(event -> about());

        g.menuConnect.setOnAction(event -> g.commandCtl.connect());
        g.menuDisconnect.setOnAction(event -> g.commandCtl.kill("menu"));

        g.menuCmdCOM.setOnAction(event -> g.commandCtl.cmdSelectCom());
        g.menuCmdTCP.setOnAction(event -> g.commandCtl.cmdSelectTCP());
        g.menuListenerCOM.setOnAction(event -> g.commandCtl.listenerSelectCom());
        g.menuListenerTCP.setOnAction(event -> g.commandCtl.listenerSelectTCP());
    }

    private void exit() {
        if (XGuiCtrl.injekt.getGsm().hasViableRightChannel()) {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit");
            alert.setHeaderText("Do you really want to exit the application?");
            alert.setContentText("This will end all the active or on-going connections");
            final Optional<ButtonType> result = alert.showAndWait();
            if (result.orElse(null) == ButtonType.OK) {
                log.info("user confirmed exit");
                System.exit(0);
            }
        }
        else {
            log.info("no active client, not asking user for exit, exit immediately");
            System.exit(0);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void about() {
        final String say = "Authored by:\n" +
            "Koosha Hosseiny i@koosha.io\n" +
            "\n\n\n"
            + "Copper Carbonate Version " + CopperCarbonateApplication.VERSION;
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Massrelay");
        alert.setHeaderText("Massrelay");
        alert.setContentText(say);
        alert.setHeight(100);
        alert.show();
    }

}
