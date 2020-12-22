package io.koosha.massrelay.beryllium.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "z_client_request_history")
public class HLeftClientRequestHistory {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false,
            updatable = false)
    private Long timestamp = System.currentTimeMillis();


    @ManyToOne(optional = false)
    private Client client;

    @Column(nullable = false)
    private Long time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventTimeType type;


    public static HLeftClientRequestHistory start(final Client client,
                                                  final long time) {
        final HLeftClientRequestHistory r = new HLeftClientRequestHistory();
        r.setClient(client);
        r.setTime(time);
        r.setType(EventTimeType.START);
        return r;
    }

    public static HLeftClientRequestHistory end(final Client client,
                                                final long time) {
        final HLeftClientRequestHistory r = new HLeftClientRequestHistory();
        r.setClient(client);
        r.setTime(time);
        r.setType(EventTimeType.END);
        return r;
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

    public Client getClient() {
        return this.client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }

    public Long getTime() {
        return this.time;
    }

    public void setTime(final Long time) {
        this.time = time;
    }

    public EventTimeType getType() {
        return this.type;
    }

    public void setType(final EventTimeType type) {
        this.type = type;
    }


    public String toString() {
        return "HLeftClientRequestHistory(id=" + this.getId() +
            ", timestamp=" + this.getTimestamp() +
            ", client=" + this.getClient() +
            ", time=" + this.getTime() +
            ", type=" + this.getType() +
            ")";
    }

}
