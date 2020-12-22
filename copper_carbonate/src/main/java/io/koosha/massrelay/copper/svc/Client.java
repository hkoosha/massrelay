package io.koosha.massrelay.copper.svc;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public final class Client {

    private final StringProperty idProperty = new SimpleStringProperty();


    public Client(final String id) {
        this.idProperty.set(id);
    }


    public String getId() {
        return this.idProperty.get();
    }

    public void setId(final String value) {
        this.idProperty.set(value);
    }

    public StringProperty getIdProperty() {
        return this.idProperty;
    }


    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Client))
            return false;
        final Client other = (Client) o;
        return Objects.equals(this.getIdProperty(), other.getIdProperty());
    }

    public int hashCode() {
        return this.getIdProperty().hashCode();
    }

}
