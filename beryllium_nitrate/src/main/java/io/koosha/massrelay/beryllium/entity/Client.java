package io.koosha.massrelay.beryllium.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Client {

    @Id
    private String id;

    @Column(nullable = false,
            updatable = false)
    private Long timestamp = System.currentTimeMillis();


    @Column(nullable = false,
            updatable = false)
    private String name;

    @Column(nullable = false,
            updatable = false)
    private String password;

    @Column(nullable = false,
            updatable = false)
    private Boolean enabled;


    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }


    public String toString() {
        return "Client(id=" + this.getId() +
            ", timestamp=" + this.getTimestamp() +
            ", name=" + this.getName() +
            ", password=???" +
            ", enabled=" + this.getEnabled() +
            ")";
    }

}
