package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.Set;

@Mixin(value = KeyBindsList.class, priority = 10000)
public abstract class KeyBindsListMixin {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lorg/apache/commons/lang3/ArrayUtils;clone([Ljava/lang/Object;)[Ljava/lang/Object;",
            remap = false
        )
    )
    private Object[] vanillaVideoSettings$filterModdedKeybinds(Object[] array) {
        Object[] cloned = ArrayUtils.clone(array);
        Set<String> hidden = ModConfig.get().hiddenKeybinds;
        if (hidden == null || hidden.isEmpty() || !(cloned instanceof KeyMapping[] keyMappings)) {
            return cloned;
        }

        return Arrays.stream(keyMappings)
            .filter(key -> key != null && !hidden.contains(key.getName()))
            .toArray(KeyMapping[]::new);
    }
}