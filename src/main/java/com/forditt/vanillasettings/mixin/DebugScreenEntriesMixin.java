package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(value = DebugScreenEntries.class, priority = 10000)
public abstract class DebugScreenEntriesMixin {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "allEntries", at = @At("RETURN"), cancellable = true, require = 0)
    private static void vanillaVideoSettings$filterModdedDebugEntries(CallbackInfoReturnable cir) {
        if (!ModConfig.get().vanillaDebugScreen) {
            return;
        }

        Map original = (Map) cir.getReturnValue();
        if (original == null || original.isEmpty()) {
            return;
        }

        Map vanillaOnly = new LinkedHashMap();
        for (Object obj : original.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            if (entry.getKey() instanceof Identifier id && "minecraft".equals(id.getNamespace())) {
                vanillaOnly.put(id, entry.getValue());
            }
        }
        cir.setReturnValue(vanillaOnly);
    }

    @SuppressWarnings("rawtypes")
    @Inject(method = "getEntry", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vanillaVideoSettings$blockModdedEntryRender(Identifier id, CallbackInfoReturnable cir) {
        if (!ModConfig.get().vanillaDebugScreen) {
            return;
        }

        if (id != null && !"minecraft".equals(id.getNamespace())) {
            cir.setReturnValue(null);
        }
    }
}