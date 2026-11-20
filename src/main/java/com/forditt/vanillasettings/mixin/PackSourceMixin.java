package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Pack.class)
public abstract class PackSourceMixin {

    @Inject(method = "getDescription", at = @At("RETURN"), cancellable = true, require = 0)
    private void vanillaVideoSettings$hideBuiltinLabelDynamically(CallbackInfoReturnable<Component> cir) {
        if (!ModConfig.get().hideBuiltinPackLabel) {
            return;
        }
        Component desc = cir.getReturnValue();
        if (desc != null && desc.getContents() instanceof TranslatableContents tc && "pack.nameAndSource".equals(tc.getKey())) {
            Object[] args = tc.getArgs();
            if (args.length >= 2 && args[1] instanceof Component sourceComp
                && sourceComp.getContents() instanceof TranslatableContents sourceTc) {
                String sourceKey = sourceTc.getKey();
                if (sourceKey != null && (sourceKey.contains("builtin") || sourceKey.contains("mod"))) {
                    if (args[0] instanceof Component originalDesc) {
                        cir.setReturnValue(originalDesc);
                    }
                }
            }
        }
    }
}