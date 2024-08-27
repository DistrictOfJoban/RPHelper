package com.lx862.rphelper.data;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CustomToast implements Toast {
    private final Identifier backgroundTexture;
    private final Identifier iconTexture;
    private final int iconSize;
    private int textureWidth;
    private final int configTextureWidth;
    private final int textureHeight;
    private Text title;
    private Text description;
    public long duration;
    private final int titleColor;
    private final int descriptionColor;
    public long time;
    public boolean hidden;
    private long lastElapsed = System.currentTimeMillis();

    public CustomToast(Text title, Text description, long duration, int titleColor, int descriptionColor,
                       Identifier backgroundTexture, Identifier iconTexture, int iconSize, int textureWidth, int textureHeight) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.titleColor = titleColor;
        this.descriptionColor = descriptionColor;
        this.backgroundTexture = backgroundTexture;
        this.iconTexture = iconTexture;
        this.iconSize = iconSize;
        this.configTextureWidth = textureWidth;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.time = 0;
        this.hidden = false;
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long currentTime) {  
        RenderSystem.setShaderTexture(0, backgroundTexture);
        manager.drawTexture(matrices, 0, 0, 0, 0, getWidth(), textureHeight, textureWidth, textureHeight);

        RenderSystem.setShaderTexture(0, iconTexture);
        manager.drawTexture(matrices, 10, (textureHeight - iconSize) / 2, 0, 0, iconSize, iconSize, iconSize, iconSize);
    
        manager.getClient().textRenderer.draw(matrices, this.title, 44, 7, this.titleColor);
        manager.getClient().textRenderer.draw(matrices, this.description, 44, 18, this.descriptionColor);
    
        if (!hidden) {
            time += System.currentTimeMillis() - lastElapsed;
            lastElapsed = System.currentTimeMillis();
        }

        if (time >= duration) {
            hidden = true;
            return Visibility.HIDE;
        }
            
        return Visibility.SHOW;
    }  

    @Override
    public int getWidth() {
        int titleLength = countCharacters(title);
        int descriptionLength = countCharacters(description);
        int contentLength = Math.max(titleLength, descriptionLength);

        if (contentLength > 22 && this.textureWidth < (contentLength - 22) * 5 + configTextureWidth) {
            int extraWidth = contentLength - 22;
            this.textureWidth += extraWidth * 5;
        }
        
        return this.textureWidth;
    }
    

    @Override
    public int getHeight() {
        return textureHeight;
    }

    @Override
    public int getRequiredSpaceCount() {
        return 1;
    }

    public void setContent(Text title, Text description) {
        this.title = title;
        this.description = description;
        time = 0; // Reset toast duration, as we can download for an unknown amount of time
    }

    private int countCharacters(Text text) {
        int count = 0;
        String string = text.getString();
        for (int i = 0; i < string.length(); i++) {
            if (string.codePointAt(i) < 128) {
                count++;
            }
        }
        return count;
    }
}
