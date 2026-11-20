package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TitleScreen.class, priority = 10000)
public abstract class TitleScreenBrandingMixin extends Screen {

    @Shadow
    @Nullable
    private RealmsNotificationsScreen realmsNotificationsScreen;

    protected TitleScreenBrandingMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"), require = 0)
    private void vanillaVideoSettings$restoreRealmsNotifications(CallbackInfo ci) {
        if (!ModConfig.get().hideModMenuButtons) {
            return;
        }
        if (this.realmsNotificationsScreen == null) {
            this.realmsNotificationsScreen = new RealmsNotificationsScreen();
        }
        this.realmsNotificationsScreen.init(this.width, this.height);
    }

    @ModifyArg(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
        ),
        index = 1,
        require = 0
    )
    private String vanillaVideoSettings$cleanVersionString(String text) {
        if (!ModConfig.get().vanillaBranding || text == null) {
            return text;
        }
        if (text.startsWith("Minecraft ")) {
            int slashIdx = text.indexOf('/');
            if (slashIdx != -1) {
                return text.substring(0, slashIdx);
            }
            int parenIdx = text.indexOf(" (");
            if (parenIdx != -1) {
                return text.substring(0, parenIdx);
            }
        }
        return text;
    }

    @ModifyArg(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
        ),
        index = 1,
        require = 0
    )
    private Component vanillaVideoSettings$hideModMenuCountComponent(Component component) {
        if (!ModConfig.get().vanillaBranding || component == null) {
            return component;
        }
        if (component.getContents() instanceof TranslatableContents tc && tc.getKey().startsWith("modmenu.")) {
            return Component.empty();
        }
        String raw = component.getString();
        if (raw.contains("mod") || raw.contains("мод") || raw.contains("Mod")) {
            return Component.empty();
        }
        return component;
    }
}