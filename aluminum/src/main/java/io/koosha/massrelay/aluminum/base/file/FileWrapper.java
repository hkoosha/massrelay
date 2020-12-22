package io.koosha.massrelay.aluminum.base.file;

public interface FileWrapper {

    void write(String content);


    String read();


    boolean readonly();

    boolean readable();

}
