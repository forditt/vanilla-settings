package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Window.class)
public abstract class BrandingMixin {

    @ModifyVariable(method = "setTitle", at = @At("HEAD"), argsOnly = true, require = 0)
    private String vanillaVideoSettings$removeAsteriskFromTitle(String title) {
        if (ModConfig.get().vanillaBranding && title != null && title.startsWith("Minecraft*")) {
            return "Minecraft" + title.substring("Minecraft*".length());
        }
        return title;
    }
}