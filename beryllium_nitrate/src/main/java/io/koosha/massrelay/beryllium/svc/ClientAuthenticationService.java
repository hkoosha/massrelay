package io.koosha.massrelay.beryllium.svc;

import io.koosha.massrelay.aluminum.base.qualify.Left;
import io.koosha.massrelay.aluminum.base.security.PasswordEncoder;
import io.koosha.massrelay.beryllium.entity.Client;
import io.koosha.massrelay.beryllium.entity.ClientRepo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

@Left
@Singleton
@Component
public final class ClientAuthenticationService implements AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final ClientRepo clientRepo;

    @Inject
    public ClientAuthenticationService(final PasswordEncoder passwordEncoder,
                                       final ClientRepo clientRepo) {
        this.passwordEncoder = passwordEncoder;
        this.clientRepo = clientRepo;
    }

    @Override
    public boolean isAuthenticated(final String id,
                                   final String hash) {
        final Client client = clientRepo.findById(id).orElse(null);
        return client != null
            && client.getEnabled()
            && passwordEncoder.matches(hash, client.getPassword());
    }

}
