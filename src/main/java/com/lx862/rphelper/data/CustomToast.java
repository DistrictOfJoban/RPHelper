package com.lx862.rphelper.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
    private final int configTextureWidth;
    private final int textureHeight;
    private final int titleColor;
    private final int descriptionColor;
    private int textureWidth;
    private Text title;
    private Text description;
    private long lastElapsed = System.currentTimeMillis();
    public long duration;
    public long elapsedTime;
    public boolean hidden;

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
        this.elapsedTime = 0;
        this.hidden = false;
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        final MatrixStack matrices = context.getMatrices();
//        RenderSystem.setShaderTexture(0, backgroundTexture);
        context.drawTexture(backgroundTexture, 0, 0, 0, 0, getWidth(), textureHeight, textureWidth, textureHeight);

//        RenderSystem.setShaderTexture(0, iconTexture);
        context.drawTexture(iconTexture, 10, (textureHeight - iconSize) / 2, 0, 0, iconSize, iconSize, iconSize, iconSize);


        context.drawText(MinecraftClient.getInstance().textRenderer, this.title, 44, 7, this.titleColor, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, this.description, 44, 18, this.descriptionColor, false);

        elapsedTime += System.currentTimeMillis() - lastElapsed;
        lastElapsed = System.currentTimeMillis();

        return elapsedTime >= duration ? Visibility.HIDE : Visibility.SHOW;
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
        elapsedTime = 0; // Reset toast duration, as we can download for an unknown amount of time
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
