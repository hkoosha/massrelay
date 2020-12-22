package io.koosha.massrelay.aluminum.base.file;

public class FileServiceImpl implements FileService {

    protected FileWrapper wrapper(final String location,
                                  final String content,
                                  final boolean readOnly) {
        return new FileWrapperImpl(location, content, readOnly);
    }


    @Override
    public FileWrapper fRead(final String location) {
        return wrapper(location, null, true);
    }

    @Override
    public boolean readable(String location) {
        return wrapper(location, null, true).readable();
    }

}
