package io.koosha.massrelay.copper.gui;

import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.copper.gui.fx.ValidatedTableCell;
import io.koosha.massrelay.copper.svc.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

final class ClientCtrl {

    private final XGuiCtrl g;
    private final ObservableList<Client> clientsObs = FXCollections.observableArrayList();

    private ContextMenu tblClientsContextMenu;

    ClientCtrl(final XGuiCtrl g) {
        final ContextMenu cm = new ContextMenu();
        final MenuItem m = new MenuItem("Delete");
        m.setOnAction(e0 -> {
            this.tblClientsContextMenu.hide();
            deleteSelectedClient();
        });
        cm.getItems().add(m);
        cm.hide();
        this.tblClientsContextMenu = cm;

        this.g = g;
        this.init();
    }

    private static <E, T> String duplicates(final Collection<E> c,
                                            final Function<E, T> to) {
        final Set<T> dup = new HashSet<>();
        final Set<T> seen = new HashSet<>();
        for (final E cc : c) {
            final T ct = to.apply(cc);
            if (seen.contains(ct))
                dup.add(ct);
            else
                seen.add(ct);
        }
        return dup.stream()
           .map(Object::toString)
           .collect(Collectors.joining(", "));
    }

    private static <E, T> boolean hasDup(final Collection<E> c,
                                         final Function<E, T> to) {
        return c.stream().map(to).collect(Collectors.toSet()).size() != c.size();
    }

    @SafeVarargs
    private static <S, T> void addColumns(final TableView<S> tv,
                                          final TableColumn<S, T>... tc) {
        tv.getColumns().clear();
        for (final TableColumn<S, T> each : tc) {
            each.prefWidthProperty().bind(
                tv.widthProperty().divide(tc.length).subtract(2));
            tv.getColumns().add(each);
        }
    }

    private void init() {
        g.tblClients.setItems(clientsObs);
        g.tblClients.setOnMouseClicked(e -> {
            tblClientsContextMenu.hide();
            if (e.getButton() == MouseButton.SECONDARY) {
                double columnHeaderHeight = g.tblClients
                    .lookup(".column-header-background")
                    .getBoundsInLocal()
                    .getHeight();
                if (e.getY() > columnHeaderHeight)
                    tblClientsContextMenu.show(g.tblClients, e.getScreenX(), e.getScreenY());
            }
        });
        g.tblClients.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.DELETE)
                deleteSelectedClient();
        });
        g.tblClients.getColumns().clear();
        g.tblClients.setEditable(false);

        final TableColumn<Client, String> col = new TableColumn<>("Id");
        col.setCellValueFactory(cellData -> {
            Client client = cellData.getValue();
            return client.getIdProperty();
        });
        col.setCellFactory(tc -> new ValidatedTableCell<>(s -> false, XGuiCtrl.injekt.enabled::get));
        col.setPrefWidth(150);

        addColumns(g.tblClients, col);

        g.tfNewClientId.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER)
                addNewClient();
        });

        populateClientsList();
    }

    private void addNewClient() {
        if (!XGuiCtrl.injekt.enabled())
            return;

        final Client newClient = new Client("");
        if (g.tfNewClientId.getText().isEmpty()) {
            Rrr.error("client id can not be empty");
            return;
        }
        newClient.setId(g.tfNewClientId.getText());
        g.commandCtl.kill("new client added");
        if (XGuiCtrl.injekt.getClientService().add(newClient))
            g.tfNewClientId.setText("");
        populateClientsList();
    }

    private void saveAllClients() {
        if (!XGuiCtrl.injekt.enabled())
            return;

        if (hasDup(this.clientsObs, Client::getId)) {
            Rrr.error("Duplicate client id: " + duplicates(this.clientsObs, Client::getId));
            return;
        }
        g.commandCtl.kill("saving clients list");
        XGuiCtrl.injekt.getClientService().swap(new HashSet<>(this.clientsObs));
        populateClientsList();
    }

    private void populateClientsList() {
        clientsObs.clear();
        clientsObs.addAll(XGuiCtrl.injekt.getClientService().getAll());
    }

    private void deleteSelectedClient() {
        if (!XGuiCtrl.injekt.enabled())
            return;

        final Client client = g.tblClients.getSelectionModel().getSelectedItems().get(0);
        if (client == null)
            return;
        final List<Client> newList = this.clientsObs
            .stream()
            .filter(c -> !Objects.equals(c.getId(), client.getId()))
            .filter(c -> c.getId() != null && !c.getId().isEmpty())
            .collect(toList());
        this.clientsObs.clear();
        this.clientsObs.addAll(newList);
        this.saveAllClients();
    }

    Client getSelectedClient() {
        final Client ret = g.tblClients.getSelectionModel().getSelectedItems().get(0);
        return ret == null || (ret.getId() == null || ret.getId().isEmpty()) ? null : ret;
    }

}
