package com.lx862.rphelper.data;

import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

public class PackEntry {
    public final URL sourceUrl;
    public final @Nullable String hash;
    public final @Nullable URL hashUrl;
    public final String name;
    public final String[] equivPack;
    public boolean ready = false;
    private final String fileName;

    public PackEntry(@Nullable String name, String sourceUrl, @Nullable String hash, @Nullable String hashUrl, String fileName, String[] equivPack) throws MalformedURLException {
        if(sourceUrl == null || fileName == null) throw new IllegalArgumentException("sourceURL and fileName must not be null!");
        this.sourceUrl = new URL(sourceUrl);
        this.hash = hash;
        this.hashUrl = hashUrl == null ? null : new URL(hashUrl);
        this.fileName = fileName;
        this.equivPack = equivPack;
        this.name = name == null ? fileName : name;
    }

    public String uniqueId() {
        return this.sourceUrl + (this.hashUrl == null ? this.hash : this.hashUrl.toString()) + this.fileName;
    }

    public String getFileName() {
        return Paths.get(fileName).getFileName().toString();
    }

    public void logInfo(String content) {
        Log.info("[" + name + "] " + content);
    }

    public void logWarn(String content) {
        Log.warn("[" + name + "] " + content);
    }

    public void logError(String content) {
        Log.error("[" + name + "] " + content);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PackEntry)) return false;
        if(obj == this) return true;

        return Objects.equals(this.uniqueId(), ((PackEntry) obj).uniqueId());
    }
}
