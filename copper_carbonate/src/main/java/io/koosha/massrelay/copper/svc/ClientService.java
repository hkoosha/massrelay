package io.koosha.massrelay.copper.svc;

import java.util.Collection;
import java.util.Set;

public interface ClientService {

    Set<Client> getAll();

    Client getOrNull(String id);

    boolean exists(String id);

    boolean add(Client client);

    void swap(Collection<Client> with);

}


