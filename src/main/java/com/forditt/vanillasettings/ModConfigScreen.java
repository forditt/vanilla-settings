package com.forditt.vanillasettings;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ModConfigScreen extends Screen {
    private final Screen parent;

    public ModConfigScreen(Screen parent) {
        super(Component.translatableWithFallback("vanillasettings.config.title", "Vanilla Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig config = ModConfig.get();
        int buttonWidth = 260;
        int centerX = this.width / 2 - buttonWidth / 2;
        int startY = this.height / 2 - 74;

        int titleWidth = this.font.width(this.title);
        this.addRenderableWidget(
            new StringWidget((this.width - titleWidth) / 2, 12, titleWidth, 12, this.title, this.font)
        );

        // Кнопка 1: Ванільні налаштування графіки
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.vanillaVideoSettings)
                .create(
                    centerX,
                    startY,
                    buttonWidth,
                    20,
                    Component.translatableWithFallback("vanillasettings.config.vanilla_video_settings", "Vanilla Video Settings"),
                    (button, value) -> {
                        config.vanillaVideoSettings = value;
                        ModConfig.save();
                    }
                )
        );

        // Кнопка 2: Ванільний F3
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.vanillaDebugScreen)
                .create(
                    centerX,
                    startY + 24,
                    buttonWidth,
                    20,
                    Component.translatableWithFallback("vanillasettings.config.vanilla_debug_screen", "Vanilla F3 Screen"),
                    (button, value) -> {
                        config.vanillaDebugScreen = value;
                        ModConfig.save();
                    }
                )
        );

        // Кнопка 3: Приховати кнопки Mod Menu
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.hideModMenuButtons)
                .create(
                    centerX,
                    startY + 48,
                    buttonWidth,
                    20,
                    Component.translatableWithFallback("vanillasettings.config.hide_modmenu_buttons", "Hide Mod Menu Buttons"),
                    (button, value) -> {
                        config.hideModMenuButtons = value;
                        ModConfig.save();
                    }
                )
        );

        // Кнопка 4: Приховати «модифіковано» та зірочку
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.vanillaBranding)
                .create(
                    centerX,
                    startY + 72,
                    buttonWidth,
                    20,
                    Component.translatableWithFallback("vanillasettings.config.vanilla_branding", "Hide \"modded\" and *"),
                    (button, value) -> {
                        config.vanillaBranding = value;
                        ModConfig.save();
                        if (this.minecraft != null) {
                            this.minecraft.updateTitle();
                        }
                    }
                )
        );

        // Кнопка 5: Приховати «Вбудований» у ресурс-паках
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.hideBuiltinPackLabel)
                .create(
                    centerX,
                    startY + 96,
                    buttonWidth,
                    20,
                    Component.translatableWithFallback("vanillasettings.config.hide_builtin_pack_label", "Hide \"Built-in\" in Resource Packs"),
                    (button, value) -> {
                        config.hideBuiltinPackLabel = value;
                        ModConfig.save();
                    }
                )
        );

        // Кнопка 6: Вибір біндів
        this.addRenderableWidget(
            Button.builder(
                Component.translatableWithFallback("vanillasettings.config.keybinds", "Select Keybinds..."),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new KeybindsConfigScreen(this));
                    }
                }
            )
            .bounds(centerX, startY + 120, buttonWidth, 20)
            .build()
        );

        // Кнопка "Готово"
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 32, 200, 20)
                .build()
        );
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }
}