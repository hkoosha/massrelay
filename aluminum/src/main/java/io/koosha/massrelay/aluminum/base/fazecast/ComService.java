package io.koosha.massrelay.aluminum.base.fazecast;

public interface ComService {

    void enable();

    void disable();

    boolean isEnabled();


    void close();

    boolean open();

    boolean isOpen();

    boolean makeWritable();


    boolean setPort(SerialKonf serialKonf);

    void clearPort();

    SerialKonf getComKonf();


    boolean write(byte[] data);

    void registerToData(ComDataListener listener);


}
