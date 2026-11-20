package com.forditt.vanillasettings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class KeybindsConfigScreen extends Screen {
    private final Screen parent;
    private EditBox searchBox;
    private StringWidget statsWidget;
    private Button showAllBtn;
    private KeybindsSelectionList selectionList;

    private String searchQuery = "";
    private int totalKeysCount = 0;

    private static final int LIST_TOP = 62;
    private static final int LIST_BOTTOM_MARGIN = 40;
    private static final int ROW_HEIGHT = 24;

    public KeybindsConfigScreen(Screen parent) {
        super(Component.translatableWithFallback("vanillasettings.keybinds.title", "Select Keybinds"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Component styledTitle = this.title.copy().withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);
        int titleWidth = this.font.width(styledTitle);
        this.addRenderableWidget(
            new StringWidget((this.width - titleWidth) / 2, 10, titleWidth, 12, styledTitle, this.font)
        );

        this.statsWidget = new StringWidget(0, 23, this.width, 10, Component.empty(), this.font);
        this.addRenderableWidget(this.statsWidget);

        int searchWidth = Math.min(340, this.width - 40);
        this.searchBox = new EditBox(
            this.font,
            this.width / 2 - searchWidth / 2,
            37,
            searchWidth,
            18,
            Component.translatableWithFallback("vanillasettings.keybinds.search_narration", "Search keybind...")
        );
        this.searchBox.setHint(
            Component.translatableWithFallback("vanillasettings.keybinds.search_hint", "Search by name or category...")
                .withStyle(ChatFormatting.DARK_GRAY)
        );
        this.searchBox.setValue(this.searchQuery);
        this.searchBox.setFocused(false);
        this.searchBox.setResponder(text -> {
            this.searchQuery = text;
            if (this.selectionList != null) {
                this.selectionList.reloadEntries();
            }
        });
        this.addRenderableWidget(this.searchBox);

        int listHeight = Math.max(40, this.height - LIST_TOP - LIST_BOTTOM_MARGIN);
        this.selectionList = new KeybindsSelectionList(this.minecraft, this.width, listHeight, LIST_TOP, ROW_HEIGHT);
        this.addRenderableWidget(this.selectionList);

        int bottomY = this.height - 28;
        int btnWidth = 140;
        int gap = 10;
        int startBottomX = this.width / 2 - (btnWidth * 2 + gap) / 2;

        ModConfig config = ModConfig.get();
        this.showAllBtn = Button.builder(
            Component.translatableWithFallback("vanillasettings.keybinds.show_all", "Show All"),
            btn -> {
                config.hiddenKeybinds.clear();
                ModConfig.save();
                updateStatsLabel();
                if (this.selectionList != null) {
                    this.selectionList.refreshButtons();
                }
            }
        ).bounds(startBottomX, bottomY, btnWidth, 20).build();
        this.addRenderableWidget(this.showAllBtn);

        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, btn -> this.onClose())
                .bounds(startBottomX + btnWidth + gap, bottomY, btnWidth, 20)
                .build()
        );

        updateStatsLabel();
    }

    private void updateStatsLabel() {
        if (this.statsWidget == null) return;
        ModConfig config = ModConfig.get();
        int activeCount = Math.max(0, totalKeysCount - config.hiddenKeybinds.size());
        Component statsText = Component.translatableWithFallback("vanillasettings.keybinds.stats", "Active: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(activeCount + " / " + totalKeysCount).withStyle(ChatFormatting.WHITE));
        int statsWidth = this.font.width(statsText);
        this.statsWidget.setX((this.width - statsWidth) / 2);
        this.statsWidget.setWidth(statsWidth);
        this.statsWidget.setMessage(statsText);

        if (this.showAllBtn != null) {
            this.showAllBtn.active = !config.hiddenKeybinds.isEmpty();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.searchBox != null && !this.searchBox.isMouseOver(event.x(), event.y())) {
            this.searchBox.setFocused(false);
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }

    private String trimToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = this.font.width(ellipsis);
        String trimmed = this.font.plainSubstrByWidth(text, Math.max(0, maxWidth - ellipsisWidth));
        return trimmed + ellipsis;
    }

    private class KeybindsSelectionList extends ContainerObjectSelectionList<KeybindEntry> {

        public KeybindsSelectionList(Minecraft mc, int width, int height, int y, int itemHeight) {
            super(mc, width, height, y, itemHeight);
            reloadEntries();
        }

        public void reloadEntries() {
            this.clearEntries();
            this.setScrollAmount(0.0);

            if (this.minecraft.options == null || this.minecraft.options.keyMappings == null) {
                totalKeysCount = 0;
                return;
            }

            KeyMapping[] allKeys = ArrayUtils.clone(this.minecraft.options.keyMappings);
            totalKeysCount = (int) Arrays.stream(allKeys).filter(k -> k != null).count();

            Arrays.sort(allKeys, Comparator.comparing((KeyMapping k) -> k.getCategory().label().getString())
                .thenComparing(k -> Component.translatable(k.getName()).getString()));

            String q = searchQuery.trim().toLowerCase(Locale.ROOT);
            List<KeyMapping> filtered = Arrays.stream(allKeys)
                .filter(k -> k != null)
                .filter(k -> {
                    if (q.isEmpty()) return true;
                    String name = Component.translatable(k.getName()).getString().toLowerCase(Locale.ROOT);
                    String cat = k.getCategory().label().getString().toLowerCase(Locale.ROOT);
                    return name.contains(q) || cat.contains(q) || k.getName().toLowerCase(Locale.ROOT).contains(q);
                })
                .toList();

            String lastCategory = null;
            for (KeyMapping key : filtered) {
                String cat = key.getCategory().label().getString();
                if (!cat.equals(lastCategory)) {
                    this.addEntry(new CategoryEntry(cat));
                    lastCategory = cat;
                }
                this.addEntry(new KeyEntry(key));
            }
        }

        public void refreshButtons() {
            for (KeybindEntry entry : this.children()) {
                if (entry instanceof KeyEntry keyEntry) {
                    keyEntry.updateAppearance();
                }
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(360, this.width - 40);
        }
    }

    private abstract static class KeybindEntry extends ContainerObjectSelectionList.Entry<KeybindEntry> {
    }

    private class CategoryEntry extends KeybindEntry {
        private final StringWidget titleWidget;

        public CategoryEntry(String categoryName) {
            Component title = Component.literal("▸ " + categoryName)
                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
            int w = font.width(title);
            this.titleWidget = new StringWidget(0, 0, w, 12, title, font);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            int h = this.getContentHeight();

            this.titleWidget.setX(x + 4);
            this.titleWidget.setY(y + (h - 9) / 2 + 1);
            this.titleWidget.setWidth(Math.min(font.width(this.titleWidget.getMessage()), w - 8));
            this.titleWidget.extractRenderState(graphics, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
    }

    private class KeyEntry extends KeybindEntry {
        private final KeyMapping key;
        private final StringWidget labelWidget;
        private final Button toggleButton;

        private static final int BTN_WIDTH = 80;
        private static final int BTN_HEIGHT = 20;

        public KeyEntry(KeyMapping key) {
            this.key = key;
            this.labelWidget = new StringWidget(0, 0, 100, 12, Component.empty(), font);
            this.toggleButton = Button.builder(Component.empty(), btn -> {
                ModConfig config = ModConfig.get();
                if (config.hiddenKeybinds.contains(this.key.getName())) {
                    config.hiddenKeybinds.remove(this.key.getName());
                } else {
                    config.hiddenKeybinds.add(this.key.getName());
                }
                ModConfig.save();
                updateStatsLabel();
                updateAppearance();
            }).bounds(0, 0, BTN_WIDTH, BTN_HEIGHT).build();

            updateAppearance();
        }

        public void updateAppearance() {
            boolean isHidden = ModConfig.get().hiddenKeybinds.contains(this.key.getName());
            this.toggleButton.setMessage(
                isHidden
                    ? Component.translatableWithFallback("vanillasettings.keybinds.disable", "Disable").withStyle(ChatFormatting.RED)
                    : Component.translatableWithFallback("vanillasettings.keybinds.show", "Show").withStyle(ChatFormatting.GREEN)
            );
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            int h = this.getContentHeight();

            int boxTop = y + 1;
            int boxBottom = y + h - 1;
            boolean isHidden = ModConfig.get().hiddenKeybinds.contains(this.key.getName());
            int bgColor = hovered ? 0x33FFFFFF : 0x16FFFFFF;
            graphics.fill(x, boxTop, x + w, boxBottom, bgColor);

            int btnX = x + w - BTN_WIDTH;
            int btnY = boxTop + ((boxBottom - boxTop) - BTN_HEIGHT) / 2;
            this.toggleButton.setX(btnX);
            this.toggleButton.setY(btnY);
            this.toggleButton.extractRenderState(graphics, mouseX, mouseY, delta);

            String rawName = Component.translatable(this.key.getName()).getString();
            int maxLabelWidth = (btnX - x) - 14;
            String trimmed = trimToWidth(rawName, maxLabelWidth);

            Component styledName = Component.literal(trimmed)
                .withStyle(isHidden ? ChatFormatting.GRAY : ChatFormatting.WHITE);
            int actualWidth = Math.min(font.width(styledName), maxLabelWidth);

            this.labelWidget.setMessage(styledName);
            this.labelWidget.setX(x + 8);
            this.labelWidget.setY(btnY + (BTN_HEIGHT - 9) / 2);
            this.labelWidget.setWidth(actualWidth);
            this.labelWidget.extractRenderState(graphics, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.toggleButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.toggleButton);
        }
    }
}