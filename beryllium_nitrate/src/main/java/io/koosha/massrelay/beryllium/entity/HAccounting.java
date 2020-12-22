package io.koosha.massrelay.beryllium.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "z_accounting")
public class HAccounting {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false,
            updatable = false)
    private Long timestamp = System.currentTimeMillis();


    @Column(nullable = false,
            updatable = false)
    private Long totalWrittenBytes;

    @Column(nullable = false,
            updatable = false)
    private Long totalReadBytes;

    @ManyToOne
    private Client client;


    public static HAccounting create(final Client client,
                                     final long write,
                                     final long read) {
        final HAccounting a = new HAccounting();
        a.setClient(client);
        a.setTotalWrittenBytes(write);
        a.setTotalReadBytes(read);
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

    public Long getTotalWrittenBytes() {
        return this.totalWrittenBytes;
    }

    public void setTotalWrittenBytes(final Long totalWrittenBytes) {
        this.totalWrittenBytes = totalWrittenBytes;
    }

    public Long getTotalReadBytes() {
        return this.totalReadBytes;
    }

    public void setTotalReadBytes(final Long totalReadBytes) {
        this.totalReadBytes = totalReadBytes;
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }


    public String toString() {
        return "HAccounting(id=" + this.getId() +
            ", timestamp=" + this.getTimestamp() +
            ", totalWrittenBytes=" + this.getTotalWrittenBytes() +
            ", totalReadBytes=" + this.getTotalReadBytes() +
            ", client=" + this.getClient() +
            ")";
    }

}
