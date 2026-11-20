package com.forditt.vanillasettings.mixin;

import com.forditt.vanillasettings.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Mixin(value = DebugScreenOverlay.class, priority = 10000)
public abstract class DebugScreenOverlayMixin {

    @Unique
    private static List<Pattern> modTokenPatterns = null;

    @Unique
    private static final Set<String> SYSTEM_MOD_IDS = Set.of(
        "minecraft", "java", "fabricloader", "mixinextras"
    );

    @Unique
    private static List<Pattern> getModTokenPatterns() {
        if (modTokenPatterns != null) {
            return modTokenPatterns;
        }
        Set<String> rawTokens = new HashSet<>();
        rawTokens.add("fabric");
        rawTokens.add("modded");

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String id = mod.getMetadata().getId().toLowerCase(Locale.ROOT).trim();
            String name = mod.getMetadata().getName().toLowerCase(Locale.ROOT).trim();

            if (SYSTEM_MOD_IDS.contains(id) || id.startsWith("fabric-")) {
                continue;
            }
            if (id.length() >= 4) {
                rawTokens.add(id);
            }
            if (name.length() >= 4 && !name.startsWith("fabric ")) {
                rawTokens.add(name);
            }
        }

        List<Pattern> patterns = new ArrayList<>(rawTokens.size());
        for (String token : rawTokens) {
            // Шукаємо токен мода тільки як ціле слово (наприклад, "[Sodium]", "Voxy:", "Lithium"),
            // а не як частину іншого слова
            patterns.add(Pattern.compile("(?<![a-z0-9_])" + Pattern.quote(token) + "(?![a-z0-9_])"));
        }
        modTokenPatterns = patterns;
        return modTokenPatterns;
    }

    @Unique
    private static boolean isSystemHardwareLine(String line, int index, List<String> allLines) {
        if (line.startsWith("Minecraft ")
            || line.startsWith("Java:")
            || line.startsWith("Mem:")
            || line.startsWith("Allocation rate:")
            || line.startsWith("Native:")
            || line.startsWith("CPU:")
            || line.startsWith("Display:")) {
            return true;
        }
        // Рядки GPU та драйвера OpenGL/Vulkan завжди йдуть 1-м і 2-м рядком одразу після "Display:"
        if (index >= 1 && allLines.get(index - 1) != null && allLines.get(index - 1).startsWith("Display:")) {
            return true;
        }
        if (index >= 2 && allLines.get(index - 2) != null && allLines.get(index - 2).startsWith("Display:")) {
            return true;
        }
        return false;
    }

    @ModifyVariable(
        method = "extractLines",
        at = @At("HEAD"),
        argsOnly = true,
        require = 0
    )
    private List<String> vanillaVideoSettings$cleanExtractedLines(List<String> lines) {
        if (!ModConfig.get().vanillaDebugScreen || lines == null || lines.isEmpty()) {
            return lines;
        }

        // 1. Очищаємо версію гри від приписок Fabric / modded
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null) continue;
            if (line.startsWith("Minecraft ") || line.contains("Integrated server") || line.contains("Multiplayer")) {
                String cleaned = line
                    .replace("/fabric", "")
                    .replace(" (modded)", "")
                    .replace(" (Fabric)", "");
                lines.set(i, cleaned);
            }
        }

        // 2. Проходимо по всіх рядках із урахуванням індексу (щоб не зачепити блок GPU під Display:)
        List<Pattern> patterns = getModTokenPatterns();
        List<String> filtered = new ArrayList<>(lines.size());

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null) continue;

            if (isSystemHardwareLine(line, i, lines)) {
                filtered.add(line);
                continue;
            }

            String lower = line.toLowerCase(Locale.ROOT);
            boolean isModLine = false;
            for (Pattern p : patterns) {
                if (p.matcher(lower).find()) {
                    isModLine = true;
                    break;
                }
            }

            if (!isModLine) {
                filtered.add(line);
            }
        }

        lines.clear();
        lines.addAll(filtered);
        return lines;
    }
}