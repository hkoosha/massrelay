package io.koosha.massrelay.aluminum.base.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("ClassCanBeRecord")
final class FileWrapperImpl implements FileWrapper {

    private static final Logger log = LoggerFactory.getLogger(FileWrapperImpl.class);

    private final String location;
    private final String content;
    private final boolean readonly;

    FileWrapperImpl(final String location,
                    final String content,
                    final boolean readonly) {
        if (content != null && !readonly)
            throw new IllegalArgumentException("in memory file must be read only");
        this.location = location;
        this.content = content;
        this.readonly = readonly;
    }

    private void assertLocation() {
        if (this.location == null)
            throw new IllegalStateException("file has no location");
    }

    @Override
    public String read() {
        if (this.content != null)
            return this.content;

        assertLocation();

        try {
            final byte[] bytes = Files.readAllBytes(Paths.get(this.location));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public void write(final String content) {
        assertLocation();

        if (this.readonly())
            throw new IllegalStateException("read-only file");

        final String bkName = this.location + System.currentTimeMillis() + ".bk";
        final File bkFile = new File(bkName);

        try {
            if (!bkFile.createNewFile()) {
                log.error("could not create backup file: {}", bkName);
                return;
            }
            Files.writeString(bkFile.toPath(), this.read());
            Files.writeString(Paths.get(this.location), content);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean readonly() {
        return this.readonly;
    }

    @Override
    public boolean readable() {
        final File fl = new File(this.location);
        if (!fl.exists() || !fl.canRead())
            return false;

        try {
            Files.readAllBytes(Paths.get(this.location));
            return true;
        } catch (final IOException e) {
            return false;
        }
    }


    @Override
    public String toString() {
        return "File[" + this.location + ']';
    }

}
