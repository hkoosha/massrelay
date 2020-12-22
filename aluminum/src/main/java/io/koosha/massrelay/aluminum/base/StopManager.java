package io.koosha.massrelay.aluminum.base;

import io.koosha.massrelay.aluminum.base.func.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static io.koosha.massrelay.aluminum.base.Util.doAllOf;

public final class StopManager {

    private final Object LOCK = new Object();

    private final Collection<Action> actions = new ArrayList<>();

    private volatile boolean called = false;

    public void register(final Action... actions) {
        synchronized (LOCK) {
            Collections.addAll(this.actions, actions);
        }
    }

    public void fin() {
        synchronized (LOCK) {
            if (called)
                return;
            called = true;
            doAllOf("StopManager::Fin", actions.toArray(new Action[0]));
        }
    }

}
