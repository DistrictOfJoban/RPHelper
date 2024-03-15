package com.lx862.rphelper.custom;

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
    private static final Identifier CUSTOM_BACKGROUND_TEXTURE = new Identifier("jbrph", "textures/custom_toast.png");
    private Text title;
    private Text description;
    public long duration;
    private final int titleColor;
    private final int descriptionColor;
    public long time;
    public boolean hidden;

    public CustomToast(Text title, Text description, long duration, int titleColor, int descriptionColor) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.titleColor = titleColor;
        this.descriptionColor = descriptionColor;
        this.time = 0;
        this.hidden = false;
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long currentTime) {  
        RenderSystem.setShaderTexture(0, CUSTOM_BACKGROUND_TEXTURE);
        manager.drawTexture(matrices, 0, 0, 0, 0, 180, 34, 180, 34);
    
        manager.getClient().textRenderer.draw(matrices, this.title, 44, 7, this.titleColor);
        manager.getClient().textRenderer.draw(matrices, this.description, 44, 18, this.descriptionColor);
    
        if (hidden == false) {
        time += 1;}
        System.out.println(time);
        if (time >= duration) {
            hidden = true;
            return Visibility.HIDE;
        }
            
        return Visibility.SHOW;
    }  

    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public int getRequiredSpaceCount() {
        return 1;
    }

    public void setContent(Text title, Text description) {
        this.title = title;
        this.description = description;
    }
}
