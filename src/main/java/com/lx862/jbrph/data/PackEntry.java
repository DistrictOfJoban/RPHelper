package com.lx862.jbrph.data;

import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class PackEntry {
    public final URL sourceUrl;
    public final @Nullable URL hashUrl;
    public final String fileName;
    public final String name;

    public PackEntry(@Nullable String name, String sourceUrl, @Nullable String hashUrl, String fileName) throws MalformedURLException {
        if(sourceUrl == null || fileName == null) throw new IllegalArgumentException("sourceURL and fileName must not be null!");
        this.sourceUrl = new URL(sourceUrl);
        this.hashUrl = hashUrl == null ? null : new URL(hashUrl);
        this.fileName = fileName;
        this.name = name == null ? fileName : name;
    }

    public String uniqueId() {
        return this.sourceUrl + (this.hashUrl == null ? "" : this.hashUrl.toString()) + this.fileName;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PackEntry)) return false;
        if(obj == this) return true;

        return Objects.equals(this.uniqueId(), ((PackEntry) obj).uniqueId());
    }
}
