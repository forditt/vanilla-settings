package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(PackSelectionModel.class)
public abstract class PackSelectionModelMixin {

    @Unique
    private static final Set<String> VANILLA_BUILTIN_PACKS = Set.of(
        "vanilla",
        "programmer_art",
        "high_contrast",
        "fabric"
    );

    @Unique
    private static boolean isHiddenModBuiltinPack(PackSelectionModel.Entry entry) {
        if (entry == null) return false;
        String id = entry.getId();
        if (id.startsWith("file/") || id.startsWith("server/") || VANILLA_BUILTIN_PACKS.contains(id)) {
            return false;
        }
        return true;
    }

    @Inject(method = "getUnselected", at = @At("RETURN"), cancellable = true, require = 0)
    private void vanillaVideoSettings$hideModPacksUnselected(CallbackInfoReturnable<Stream<PackSelectionModel.Entry>> cir) {
        if (!ModConfig.get().hideBuiltinPackLabel) {
            return;
        }
        Stream<PackSelectionModel.Entry> original = cir.getReturnValue();
        if (original != null) {
            List<PackSelectionModel.Entry> filtered = original
                .filter(entry -> !isHiddenModBuiltinPack(entry))
                .toList();
            cir.setReturnValue(filtered.stream());
        }
    }

    @Inject(method = "getSelected", at = @At("RETURN"), cancellable = true, require = 0)
    private void vanillaVideoSettings$hideModPacksSelected(CallbackInfoReturnable<Stream<PackSelectionModel.Entry>> cir) {
        if (!ModConfig.get().hideBuiltinPackLabel) {
            return;
        }
        Stream<PackSelectionModel.Entry> original = cir.getReturnValue();
        if (original != null) {
            List<PackSelectionModel.Entry> filtered = original
                .filter(entry -> !isHiddenModBuiltinPack(entry))
                .toList();
            cir.setReturnValue(filtered.stream());
        }
    }
}