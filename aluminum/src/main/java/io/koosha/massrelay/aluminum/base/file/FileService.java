package io.koosha.massrelay.aluminum.base.file;

public interface FileService {

    FileWrapper fRead(String location);

    default String read(String location) {
        return fRead(location).read();
    }

    boolean readable(String location);

}
