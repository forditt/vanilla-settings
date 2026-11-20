package com.forditt.vanillasettings;

import com.mojang.realmsclient.RealmsMainScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VanillaVideoSettingsMod implements ClientModInitializer {

    private static final Identifier CLEANUP_PHASE = Identifier.fromNamespaceAndPath("vanillavideosettings", "cleanup");

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, CLEANUP_PHASE);
        ScreenEvents.AFTER_INIT.register(CLEANUP_PHASE, (client, screen, scaledWidth, scaledHeight) -> {
            // Дозволяємо відкривати Mod Menu по його гарячій клавіші в головному меню та в меню паузи
            if (screen instanceof TitleScreen || screen instanceof PauseScreen) {
                ScreenKeyboardEvents.beforeKeyPress(screen).register((scr, event) -> {
                    if (isModMenuHotkeyPressed(client, event)) {
                        openModMenuScreen(client, scr);
                    }
                });
            }

            if (!ModConfig.get().hideModMenuButtons) {
                return;
            }

            if (screen instanceof TitleScreen titleScreen) {
                restoreVanillaTitleScreen(client, titleScreen, Screens.getWidgets(screen), scaledWidth, scaledHeight);
            } else if (screen instanceof PauseScreen) {
                restoreVanillaPauseScreen(Screens.getWidgets(screen), scaledWidth);
            }
        });
    }

    private static void restoreVanillaTitleScreen(Minecraft client, TitleScreen screen, List<AbstractWidget> widgets, int scaledWidth, int scaledHeight) {
        widgets.removeIf(VanillaVideoSettingsMod::isModMenuButton);

        // Перевіряємо, чи кнопки меню вже ініціалізовані на екрані
        boolean hasSingleplayer = false;
        for (AbstractWidget w : widgets) {
            if (isTranslatableKey(w.getMessage(), "menu.singleplayer")) {
                hasSingleplayer = true;
                break;
            }
        }
        if (!hasSingleplayer) return;

        // Жорстко задаємо ванільний topY (незалежно від того, чи Mod Menu зсував кнопки вгору в режимі "Під Realms")
        int topY = scaledHeight / 4 + 48;
        int iconsRowY = topY + 72;
        int bottomRowY = topY + 96;
        int centerX = scaledWidth / 2;

        List<AbstractWidget> iconButtons = new ArrayList<>();
        boolean realmsFound = false;

        for (AbstractWidget w : widgets) {
            Component msg = w.getMessage();
            if (isTranslatableKey(msg, "menu.singleplayer")) {
                w.setX(centerX - 100);
                w.setY(topY);
                w.setWidth(200);
            } else if (isTranslatableKey(msg, "menu.multiplayer")) {
                w.setX(centerX - 100);
                w.setY(topY + 24);
                w.setWidth(200);
            } else if (isTranslatableKey(msg, "menu.online")) {
                w.setX(centerX - 100);
                w.setY(topY + 48);
                w.setWidth(200);
                w.visible = true;
                w.active = true;
                realmsFound = true;
            } else if (isTranslatableKey(msg, "menu.options")) {
                w.setX(centerX - 100);
                w.setY(bottomRowY);
                w.setWidth(98);
            } else if (isTranslatableKey(msg, "menu.quit")) {
                w.setX(centerX + 2);
                w.setY(bottomRowY);
                w.setWidth(98);
            } else if (w.getWidth() == 20 && w.getHeight() == 20) {
                iconButtons.add(w);
            }
        }

        // Якщо Mod Menu в режимі "Замість Realms" не додав ванільну кнопку Realms — створюємо її самі
        if (!realmsFound) {
            Button realmsButton = Button.builder(
                    Component.translatable("menu.online"),
                    btn -> client.setScreenAndShow(new RealmsMainScreen(screen))
                )
                .bounds(centerX - 100, topY + 48, 200, 20)
                .build();
            widgets.add(realmsButton);
        }

        if (!iconButtons.isEmpty()) {
            iconButtons.sort(Comparator.comparingInt(AbstractWidget::getX));
            int gap = 4;
            int totalIconsWidth = iconButtons.size() * 20 + (iconButtons.size() - 1) * gap;
            int startIconX = centerX - totalIconsWidth / 2;

            for (int i = 0; i < iconButtons.size(); i++) {
                AbstractWidget icon = iconButtons.get(i);
                icon.setX(startIconX + i * (20 + gap));
                icon.setY(iconsRowY);
            }
        }
    }

    private static void restoreVanillaPauseScreen(List<AbstractWidget> widgets, int scaledWidth) {
        widgets.removeIf(VanillaVideoSettingsMod::isModMenuButton);

        int topY = -1;
        for (AbstractWidget w : widgets) {
            if (isTranslatableKey(w.getMessage(), "menu.returnToGame")) {
                topY = w.getY();
                break;
            }
        }
        if (topY == -1) return;

        int centerX = scaledWidth / 2;

        AbstractWidget serverLinksBtn = null;
        AbstractWidget optionsBtn = null;
        AbstractWidget rightOfOptionsBtn = null;
        AbstractWidget disconnectBtn = null;
        List<AbstractWidget> iconButtons = new ArrayList<>();

        for (AbstractWidget w : widgets) {
            Component msg = w.getMessage();

            if (isTranslatableKey(msg, "menu.returnToGame")) {
                w.setX(centerX - 102);
                w.setY(topY);
                w.setWidth(204);
            } else if (isTranslatableKey(msg, "gui.advancements")) {
                w.setX(centerX - 102);
                w.setY(topY + 24);
                w.setWidth(98);
            } else if (isTranslatableKey(msg, "gui.stats")) {
                w.setX(centerX + 4);
                w.setY(topY + 24);
                w.setWidth(98);
            } else if (isTranslatableKey(msg, "menu.server_links") || msg.getString().contains("посилання")) {
                serverLinksBtn = w;
            } else if (isTranslatableKey(msg, "menu.options")) {
                optionsBtn = w;
            } else if (isTranslatableKey(msg, "menu.returnToMenu") || isTranslatableKey(msg, "menu.disconnect")) {
                disconnectBtn = w;
            } else if (w.getWidth() <= 24 && w.getHeight() == 20) {
                iconButtons.add(w);
            } else if (w.getY() > topY + 24 && w.getWidth() < 150) {
                rightOfOptionsBtn = w;
            }
        }

        int currentY = topY + 48;

        // 3. Ряд із 4 квадратних іконок (20x20) рівно по центру
        if (!iconButtons.isEmpty()) {
            iconButtons.sort(Comparator.comparingInt(AbstractWidget::getX));
            int gap = 4;
            int totalIconsWidth = iconButtons.size() * 20 + (iconButtons.size() - 1) * gap;
            int startIconX = centerX - totalIconsWidth / 2;

            for (int i = 0; i < iconButtons.size(); i++) {
                AbstractWidget icon = iconButtons.get(i);
                icon.setWidth(20);
                icon.setX(startIconX + i * (20 + gap));
                icon.setY(currentY);
            }
            currentY += 24;
        }

        // 4. Широка кнопка "Серверні посилання..." (204px), якщо вона є
        if (serverLinksBtn != null) {
            serverLinksBtn.setX(centerX - 102);
            serverLinksBtn.setY(currentY);
            serverLinksBtn.setWidth(204);
            currentY += 24;
        }

        // 5. Налаштування... (98px зліва) + Налашт-ня світу... / Відкрити для мережі (98px справа)
        if (optionsBtn != null) {
            if (rightOfOptionsBtn != null) {
                optionsBtn.setX(centerX - 102);
                optionsBtn.setY(currentY);
                optionsBtn.setWidth(98);

                rightOfOptionsBtn.setX(centerX + 4);
                rightOfOptionsBtn.setY(currentY);
                rightOfOptionsBtn.setWidth(98);
            } else {
                optionsBtn.setX(centerX - 102);
                optionsBtn.setY(currentY);
                optionsBtn.setWidth(204);
            }
            currentY += 24;
        }

        // 6. Від'єднатися / Зберегти і вийти до меню (204px)
        if (disconnectBtn != null) {
            disconnectBtn.setX(centerX - 102);
            disconnectBtn.setY(currentY);
            disconnectBtn.setWidth(204);
        }
    }

    private static boolean isModMenuButton(AbstractWidget widget) {
        if (widget == null) return false;
        Component msg = widget.getMessage();
        if (msg == null) return false;

        if (isTranslatableKey(msg, "modmenu.title")) {
            return true;
        }

        String text = msg.getString().trim();
        return text.equals("Mods")
            || text.equals("Моди")
            || text.equals("Моды")
            || text.startsWith("Mods (")
            || text.startsWith("Моди (")
            || text.startsWith("Моды (");
    }

    private static boolean isTranslatableKey(Component component, String key) {
        if (component != null && component.getContents() instanceof TranslatableContents tc) {
            return key.equals(tc.getKey());
        }
        return false;
    }

    private static boolean isModMenuHotkeyPressed(Minecraft client, net.minecraft.client.input.KeyEvent event) {
        if (client.options == null || client.options.keyMappings == null) return false;
        for (KeyMapping key : client.options.keyMappings) {
            if (key != null && "key.modmenu.open_menu".equals(key.getName())) {
                return key.matches(event);
            }
        }
        return false;
    }

    private static void openModMenuScreen(Minecraft client, Screen parent) {
        try {
            Class<?> modsScreenClass = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
            Constructor<?> ctor = modsScreenClass.getConstructor(Screen.class);
            Screen modsScreen = (Screen) ctor.newInstance(parent);
            client.setScreenAndShow(modsScreen);
        } catch (Exception ignored) {
        }
    }
}