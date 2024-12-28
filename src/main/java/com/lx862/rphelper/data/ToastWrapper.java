package com.lx862.rphelper.data;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import com.lx862.rphelper.config.Config;

/**
 * This class is a wrapper to a toast, as doing it the regular way would crash if textRenderer is not initialized yet (During launch) */
public class ToastWrapper {
    private final Text title;
    private final Text description;
    private final boolean error;
    public CustomToast build;

    public ToastWrapper(Text title, @Nullable Text description, boolean error) {
        this.title = title;
        this.description = description;
        this.build = null;
        this.error = error;
    }

    /**
     * Convert into a usable toast that can be added to Minecraft
     */
	public CustomToast build() {
        if(this.build == null) {
            this.build = new CustomToast(title, description, Config.getDuration(), error ? Config.getErrorTitleColor() : Config.getNormalTitleColor(), error ? Config.getErrorDescriptionColor() : Config.getNormalDescriptionColor(), error ? Config.getErrorTexture() : Config.getNormalTexture(), Config.getIconTexture(), Config.getIconSize(), Config.getWidth(), Config.getHeight());
        }
        return build;
    }
}
