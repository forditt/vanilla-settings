package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VideoSettingsScreen.class, remap = false)
public abstract class SodiumVideoSettingsScreenMixin {

    @Inject(method = "createScreen", at = @At("HEAD"), cancellable = true)
    private static void vanillaVideoSettings$useVanillaScreen(Screen prevScreen, CallbackInfoReturnable<Screen> cir) {
        if (!ModConfig.get().vanillaVideoSettings) {
            return;
        }

        if (prevScreen instanceof OptionsScreen optionsScreen) {
            Minecraft mc = Minecraft.getInstance();
            cir.setReturnValue(new net.minecraft.client.gui.screens.options.VideoSettingsScreen(optionsScreen, mc, mc.options));
        }
    }
}