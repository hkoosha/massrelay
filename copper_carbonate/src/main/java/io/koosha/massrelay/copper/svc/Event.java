package io.koosha.massrelay.copper.svc;


public enum Event {

    RIGHT_CONNECTING,
    RIGHT_AUTHENTICATING,
    RIGHT_CONNECTED,
    RIGHT_DISCONNECTED,

    LINE_CONNECTED,
    LINE_DISCONNECTED,

    LEFT_CONNECTED,
    LEFT_DISCONNECT,

    KILL,
    KILLED,

    THERE_TIME,
    REBOOT_TIME,

}
