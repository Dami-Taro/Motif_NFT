package src.main;

import java.nio.file.Path;

public class FileInfos {

    private final Path path;
    private final long size;

    private boolean isValid;
    private boolean isProcessed;

    public FileInfos(Path path, long size) {
        this.path = path;
        this.size = size;
        this.isValid = false;
        this.isProcessed = false;
    }

    // -------- getters --------

    public Path getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    // -------- setters --------

    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    public void setProcessed(boolean processed) {
        this.isProcessed = processed;
    }
}