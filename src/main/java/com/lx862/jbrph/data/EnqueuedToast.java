package com.lx862.jbrph.data;

import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class EnqueuedToast {
    private final SystemToast.Type type;
    private final Text title;
    private final Text description;
    public EnqueuedToast(SystemToast.Type type, Text title, @Nullable Text description) {
        this.type = type;
        this.title = title;
        this.description = description;
    }

    public SystemToast construct() {
        return new SystemToast(type, title, description);
    }
}
