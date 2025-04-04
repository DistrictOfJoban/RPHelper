package com.lx862.rphelper.data;

import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

public class PackEntry {
    private final String fileName;
    public final URL sourceUrl;
    public final @Nullable String sha1;
    public final @Nullable URL sha1Url;
    public final String name;
    public final String[] equivPacks;
    public final int order;
    private boolean ready = false;

    public PackEntry(@Nullable String name, String sourceUrl, @Nullable String sha1, @Nullable String sha1Url, String fileName, String[] equivPacks, int order) throws MalformedURLException {
        if(sourceUrl == null || fileName == null) throw new IllegalArgumentException("sourceURL and fileName must not be null!");
        this.name = name == null ? fileName : name;
        this.sourceUrl = new URL(sourceUrl);
        this.sha1 = sha1;
        this.sha1Url = sha1Url == null ? null : new URL(sha1Url);
        this.fileName = fileName;
        this.equivPacks = equivPacks;
        this.order = order;
    }

    public String uniqueId() {
        return this.sourceUrl + (this.sha1Url == null ? this.sha1 : this.sha1Url.toString()) + this.fileName;
    }

    /**
     * Set whether the pack entry is ready to be used (i.e. Fully downloaded with no issue/Equivalent pack loaded)
     */
    public void markReady(boolean ready) {
        this.ready = ready;
    }

    public String getFileName() {
        return Paths.get(fileName).getFileName().toString();
    }

    public boolean isReady() {
        return this.ready;
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
