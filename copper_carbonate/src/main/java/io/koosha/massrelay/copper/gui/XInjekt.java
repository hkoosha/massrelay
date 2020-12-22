package io.koosha.massrelay.copper.gui;

import com.google.common.eventbus.EventBus;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.massrelay.copper.err.Rrr;
import io.koosha.massrelay.aluminum.base.fazecast.ComService;
import io.koosha.massrelay.aluminum.base.StopManager;
import io.koosha.massrelay.copper.Names;
import io.koosha.massrelay.copper.handler.ServerDialerService;
import io.koosha.massrelay.copper.svc.ClientService;
import io.koosha.massrelay.copper.svc.GlobalStateManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

@Lazy
@Component
public final class XInjekt {

    public final AtomicBoolean enabled = new AtomicBoolean(true);

    private final ScheduledExecutorService cron;
    private final String title;
    private final ClientService clientService;
    private final EventBus bus;
    private final ComService comService;
    private final Preferences comPref;
    private final Preferences loginPref;
    private final Preferences settings;
    private final StopManager stopManager;
    private final GlobalStateManager gsm;
    private final ServerDialerService serverDialerService;

    @Inject
    public XInjekt(final Konfiguration k,
                   final EventBus bus,
                   final StopManager stopManager,
                   final ClientService clientService,
                   final GlobalStateManager gsm,
                   final ComService comService,
                   final ServerDialerService serverDialerService,
                   @Named(Names.COM) final Preferences comPref,
                   @Named(Names.LOGIN) final Preferences loginPref,
                   @Named(Names.SETTINGS) final Preferences settings,
                   final ScheduledExecutorService cron) {
        this.title = k.string("gui.title").v();
        this.bus = bus;
        this.stopManager = stopManager;
        this.clientService = clientService;
        this.gsm = gsm;
        this.comService = comService;
        this.serverDialerService = serverDialerService;
        this.comPref = comPref;
        this.loginPref = loginPref;
        this.settings = settings;
        this.cron = cron;
    }

    public boolean enabled() {
        if (!enabled.get())
            Rrr.warn("disabled!");
        return enabled.get();
    }

    public boolean silenceEnabled() {
        return enabled.get();
    }

    public ScheduledExecutorService getCron() {
        return this.cron;
    }

    public String getTitle() {
        return this.title;
    }

    public ClientService getClientService() {
        return this.clientService;
    }

    public EventBus getBus() {
        return this.bus;
    }

    public ComService getComService() {
        return this.comService;
    }

    public Preferences getComPref() {
        return this.comPref;
    }

    public Preferences getLoginPref() {
        return this.loginPref;
    }

    public Preferences getSettings() {
        return this.settings;
    }

    public StopManager getStopManager() {
        return this.stopManager;
    }

    public GlobalStateManager getGsm() {
        return this.gsm;
    }

    public ServerDialerService getServerDialerService() {
        return this.serverDialerService;
    }
}
