package com.forditt.vanillasettings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("vanillasettings.json");

    public boolean vanillaVideoSettings = true;
    public boolean vanillaDebugScreen = false;
    public boolean hideModMenuButtons = false;
    public boolean vanillaBranding = false;
    public boolean hideBuiltinPackLabel = false;
    public Set<String> hiddenKeybinds = new HashSet<>();

    private static ModConfig instance;

    public static ModConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                instance = GSON.fromJson(json, ModConfig.class);
                if (instance == null) {
                    instance = new ModConfig();
                }
                if (instance.hiddenKeybinds == null) {
                    instance.hiddenKeybinds = new HashSet<>();
                }
            } catch (Exception e) {
                instance = new ModConfig();
            }
        } else {
            instance = new ModConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(get()));
        } catch (IOException ignored) {
        }
    }
}