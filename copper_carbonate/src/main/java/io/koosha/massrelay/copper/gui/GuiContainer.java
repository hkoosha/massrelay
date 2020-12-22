package io.koosha.massrelay.copper.gui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

@SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                    justification = "It's a GUI!")
public class GuiContainer extends Application {

    private static XInjekt injekt;
    private static URL fxmlLocation;
    private static Parent root;

    public static void launch(final XInjekt injekt,
                              final URL fxmlLoc,
                              final String... args) {
        fxmlLocation = fxmlLoc;
        GuiContainer.injekt = injekt;
        launch(GuiContainer.class, args);
    }

    @Override
    public void init() throws Exception {
        super.init();

        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(fxmlLocation);
        root = loader.load();

        XGuiCtrl.injekt = injekt;
        final XGuiCtrl ctrl = loader.getController();
        ctrl.customInit();
    }

    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle(injekt.getTitle());
        final Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add("stylesheet.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        injekt.getStopManager().fin();
    }

}
