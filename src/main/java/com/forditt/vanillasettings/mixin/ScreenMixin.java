package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void vanillaVideoSettings$blockShiftP(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.get().vanillaVideoSettings) {
            if (event.key() == InputConstants.KEY_P && (event.modifiers() & 1) != 0) {
                cir.setReturnValue(true);
            }
        }
    }
}