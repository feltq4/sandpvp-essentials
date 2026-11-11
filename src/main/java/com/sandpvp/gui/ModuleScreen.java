package com.sandpvp.gui;

import com.sandpvp.Config;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings of one feature: ON/OFF switches and number settings (slider plus - and + buttons).
 * Long lists are split into pages so every button stays big enough for a touchscreen.
 */
final class ModuleScreen extends BaseScreen {
    private final String title;
    private final List<Config.Setting> settings;
    private int page = 0;

    ModuleScreen(Screen parent, String title, List<Config.Setting> settings) {
        super(parent, title);
        this.title = title;
        this.settings = settings;
    }

    @Override
    protected void init() {
        int left = left();
        int width = rowWidth();
        header(4, Ui.orange(title));

        int top = 28;
        int navY = this.height - 26;
        int perPage = Math.max(1, (navY - top - 4) / 24);
        int pages = Math.max(1, (settings.size() + perPage - 1) / perPage);
        page = Math.max(0, Math.min(page, pages - 1));

        int start = page * perPage;
        int end = Math.min(settings.size(), start + perPage);
        for (int i = start; i < end; i++) {
            int y = top + (i - start) * 24;
            Config.Setting setting = settings.get(i);
            if (setting instanceof Config.BoolSetting bool) {
                addBool(bool, left, y, width);
            } else if (setting instanceof Config.IntSetting number) {
                addNumber(number, left, y, width);
            }
        }

        int backW = width * 22 / 100;
        int resetW = width * 22 / 100;
        int arrowW = width * 14 / 100;
        int labelW = width - backW - resetW - arrowW * 2 - 8;
        int x = left;
        this.addRenderableWidget(Button.builder(Ui.light("Back"), btn -> back())
                .bounds(x, navY, backW, 20).build());
        x += backW + 2;
        this.addRenderableWidget(Button.builder(Ui.light("Reset"), btn -> {
            for (Config.Setting s : settings) {
                s.reset();
            }
            this.rebuildWidgets();
        }).bounds(x, navY, resetW, 20).build());
        x += resetW + 2;
        this.addRenderableWidget(Button.builder(Ui.orange("<"), btn -> {
            if (page > 0) {
                page--;
                this.rebuildWidgets();
            }
        }).bounds(x, navY, arrowW, 20).build());
        x += arrowW + 2;
        this.addRenderableWidget(Button.builder(Ui.light((page + 1) + "/" + pages), btn -> { })
                .bounds(x, navY, labelW, 20).build());
        x += labelW + 2;
        final int lastPage = pages - 1;
        this.addRenderableWidget(Button.builder(Ui.orange(">"), btn -> {
            if (page < lastPage) {
                page++;
                this.rebuildWidgets();
            }
        }).bounds(x, navY, arrowW, 20).build());
    }

    private void addBool(Config.BoolSetting setting, int left, int y, int width) {
        this.addRenderableWidget(Button.builder(Ui.toggle(setting.label, setting.value), btn -> {
            setting.value = !setting.value;
            btn.setMessage(Ui.toggle(setting.label, setting.value));
        }).bounds(left, y, width, 22).build());
    }

    private void addNumber(Config.IntSetting setting, int left, int y, int width) {
        int buttonW = 30;
        IntSlider slider = new IntSlider(left + buttonW + 2, y, width - 2 * buttonW - 4, 22, setting);
        this.addRenderableWidget(Button.builder(Ui.orange("-"), btn -> {
            setting.set(setting.value - setting.nudge());
            slider.sync();
        }).bounds(left, y, buttonW, 22).build());
        this.addRenderableWidget(slider);
        this.addRenderableWidget(Button.builder(Ui.orange("+"), btn -> {
            setting.set(setting.value + setting.nudge());
            slider.sync();
        }).bounds(left + width - buttonW, y, buttonW, 22).build());
    }
}
