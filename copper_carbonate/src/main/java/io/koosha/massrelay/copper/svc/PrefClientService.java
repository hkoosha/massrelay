package io.koosha.massrelay.copper.svc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.copper.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static io.koosha.massrelay.aluminum.base.Util.causeMsg;
import static java.util.stream.Collectors.toSet;

@Singleton
@Component
public final class PrefClientService implements ClientService {

    private static final Logger log = LoggerFactory.getLogger(PrefClientService.class);

    private static final String key = "clients";

    private final Preferences preferences;
    private final EventBus bus;

    @Inject
    public PrefClientService(@Named(Names.CLIENTS) final Preferences preferences,
                             final EventBus bus) {
        this.preferences = preferences;
        this.bus = bus;
    }

    private synchronized Set<Client> decode() {
        try {
            preferences.sync();
            return decode0(preferences.get(key, "[]"));
        }
        catch (final BackingStoreException | IOException e) {
            log.error("could not read users: {}", causeMsg(e));
            Rrr.error("could not read users: {}", causeMsg(e));
            return new HashSet<>();
        }
    }

    private void encode(final Set<Client> decode) {
        try {
            preferences.put(key, encode0(decode));
            preferences.flush();
        }
        catch (final JsonProcessingException | BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public synchronized Set<Client> getAll() {
        return Collections.unmodifiableSet(new HashSet<>(decode()));
    }

    @Override
    public synchronized Client getOrNull(final String id) {
        return decode()
            .stream()
            .filter(it -> Objects.equals(it.getId(), id))
            .findFirst()
            .orElse(null);
    }

    @Override
    public synchronized boolean exists(final String id) {
        return getOrNull(id) != null;
    }

    @Override
    public synchronized boolean add(final Client client) {
        log.info("adding client: {}", client);
        if (exists(client.getId())) {
            log.error("client already exists: {}", client);
            Rrr.error("client already exists: {}", client);
            return false;
        }

        final Set<Client> decode = this.decode();
        decode.add(client);
        encode(decode);

        log.info("client added: {}", client.getId());
        Rrr.info("client added: {}", client.getId());
        bus.post(Event.KILL);
        return true;
    }

    @Override
    public synchronized void swap(final Collection<Client> with) {
        encode(with
            .stream()
            .filter(client -> client != null && !client.getId().isEmpty())
            .collect(Collectors.toSet()));
    }


    static Set<Client> decode0(final String string) throws IOException {
        final TypeReference<Set<String>> type = new TypeReference<Set<String>>() {
        };
        final ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return om.readValue(string, type)
                 .stream()
                 .map(Client::new)
                 .collect(toSet());
    }

    static String encode0(final Collection<Client> clients) throws JsonProcessingException {
        final ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return om.writeValueAsString(clients.stream().map(Client::getId).collect(Collectors.toSet()));
    }

}
