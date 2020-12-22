package io.koosha.massrelay.beryllium.svc;

public interface AuthenticationService {

    boolean isAuthenticated(String id, String hash);

}
