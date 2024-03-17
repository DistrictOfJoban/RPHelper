package com.lx862.rphelper.config;

import net.minecraft.util.Identifier;

public class Texture {
    private final String namespace;
    private final String texturePath;

    public Texture(String namespace, String texturePath) {
        this.namespace = namespace;
        this.texturePath = texturePath;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public Identifier toIdentifier() {
        return new Identifier(namespace, texturePath);
    }
}
