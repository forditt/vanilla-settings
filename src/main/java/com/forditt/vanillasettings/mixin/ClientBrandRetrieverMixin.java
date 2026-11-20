package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientBrandRetriever.class, remap = false)
public abstract class ClientBrandRetrieverMixin {

    @Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vanillaVideoSettings$restoreVanillaBrand(CallbackInfoReturnable<String> cir) {
        if (ModConfig.get().vanillaBranding) {
            cir.setReturnValue("vanilla");
        }
    }
}