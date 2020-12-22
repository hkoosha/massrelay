package io.koosha.massrelay.aluminum.base.file;

import com.google.common.io.ByteSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class CtxedFileService extends FileServiceImpl implements FileService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @SuppressWarnings("NullableProblems")
    @Override
    public void setApplicationContext(final ApplicationContext ctx) {
        this.applicationContext = ctx;
    }

    @Override
    public boolean readable(final String location) {
        try {
            fRead(location).read();
            return true;
        }
        catch (final Exception e) {
            return false;
        }
    }

    @Override
    public FileWrapper fRead(final String location) {
        Objects.requireNonNull(location, "file_location");

        try {
            if (location.startsWith("file:") || location.startsWith("classpath:"))
                return this._read(location);
            else if (location.contains(":"))
                throw new IllegalArgumentException("don't know how to open file: " + location);

            if (this.readable("file:" + location))
                return this._read("file:" + location);
            else if (this.readable("classpath:" + location))
                return this._read("classpath:" + location);
            else
                throw new IOException("file not found: " + location);
        }
        catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private FileWrapper _read(final String location) throws IOException {
        final Resource resource = applicationContext.getResource(location);
        if (!resource.exists() || !resource.isReadable())
            throw new IllegalArgumentException("file is not readable: " + location);
        final InputStream stream = applicationContext.getResource(location).getInputStream();
        final String content = new ByteSource() {
            @Override
            public InputStream openStream() {
                return stream;
            }
        }.asCharSource(StandardCharsets.UTF_8).read();
        return wrapper(location, content, true);
    }

}
