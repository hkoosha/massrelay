package io.koosha.massrelay.copper.gui.fx;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.function.Predicate;
import java.util.function.Supplier;


public class ValidatedTableCell<S> extends TableCell<S, String> {

    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private final TextField textField;
    private final Supplier<Boolean> canEdit;

    private final BooleanProperty valid = new SimpleBooleanProperty();

    public ValidatedTableCell(final Predicate<String> validator,
                              final Supplier<Boolean> canEdit) {
        this.canEdit = canEdit;
        this.textField = new TextField();
        valid.bind(Bindings.createBooleanBinding(
            () -> textField.getText() != null && validator.test(textField.getText()),
            textField.textProperty()
        ));
        valid.addListener((obs, wasValid, isValid) -> pseudoClassStateChanged(INVALID, !isValid));
        pseudoClassStateChanged(INVALID, !valid.get());

        textField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case ENTER:
                    if (valid.get())
                        commitEdit(textField.getText());
                    break;

                case ESCAPE:
                    cancelEdit();
                    break;

                default:
                    // To make spotbugs happy :|
                    break;
            }
        });

        setGraphic(textField);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void updateItem(final String item,
                           final boolean empty) {
        if (!canEdit.get()) {
            cancelEdit();
            return;
        }
        super.updateItem(item, empty);
        setText(empty ? null : item);
        textField.setText(empty ? null : item);
        setContentDisplay(isEditing() ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void commitEdit(final String newValue) {
        if (!canEdit.get()) {
            cancelEdit();
            return;
        }
        super.commitEdit(newValue);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void startEdit() {
        if (!canEdit.get()) {
            cancelEdit();
            return;
        }
        super.startEdit();
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.selectAll();
        textField.requestFocus();
    }

}
