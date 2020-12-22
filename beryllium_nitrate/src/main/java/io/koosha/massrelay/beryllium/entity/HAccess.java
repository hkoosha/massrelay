package io.koosha.massrelay.beryllium.entity;

import io.koosha.massrelay.aluminum.base.value.Funcode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "z_access")
public class HAccess {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false,
            updatable = false)
    private Long timestamp = System.currentTimeMillis();


    @Column(nullable = false)
    private String funcode = Funcode.UNKNOWN.name();

    @Column(nullable = false)
    private PermissionEvent event;


    @ManyToOne(optional = false)
    private Client left;

    @ManyToOne(optional = false)
    private Client right;


    public static HAccess create(final Client left,
                                 final Client right,
                                 final Funcode funcode,
                                 final PermissionEvent event) {
        final HAccess a = new HAccess();
        a.setLeft(left);
        a.setRight(right);
        a.setFuncode(funcode.name());
        a.setEvent(event);
        return a;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFuncode() {
        return this.funcode;
    }

    public void setFuncode(final String funcode) {
        this.funcode = funcode;
    }

    public PermissionEvent getEvent() {
        return this.event;
    }

    public void setEvent(final PermissionEvent event) {
        this.event = event;
    }

    public Client getLeft() {
        return this.left;
    }

    public void setLeft(final Client left) {
        this.left = left;
    }

    public Client getRight() {
        return this.right;
    }

    public void setRight(final Client right) {
        this.right = right;
    }


    public String toString() {
        return "HAccess(id=" + this.getId() +
            ", timestamp=" + this.getTimestamp() +
            ", funcode=" + this.getFuncode() +
            ", event=" + this.getEvent() +
            ", left=" + this.getLeft() +
            ", right=" + this.getRight() +
            ")";
    }

}
