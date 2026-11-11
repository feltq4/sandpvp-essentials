package com.sandpvp.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** A simple list of big buttons that each open another screen. */
final class CategoryScreen extends BaseScreen {
    private record Entry(String label, Supplier<Screen> target) {}

    private final String title;
    private final List<Entry> entries = new ArrayList<>();

    CategoryScreen(Screen parent, String title) {
        super(parent, title);
        this.title = title;
    }

    void add(String label, Supplier<Screen> target) {
        entries.add(new Entry(label, target));
    }

    @Override
    protected void init() {
        int rowH = 26;
        int y = Math.max(6, (this.height - (entries.size() + 2) * rowH) / 2);
        header(y, Ui.orange(title));
        y += rowH;
        for (Entry entry : entries) {
            this.addRenderableWidget(Button.builder(Ui.light(entry.label()), btn -> open(entry.target().get()))
                    .bounds(left(), y, rowWidth(), 22).build());
            y += rowH;
        }
        this.addRenderableWidget(Button.builder(Ui.light("Back"), btn -> back())
                .bounds(left(), y, rowWidth(), 22).build());
    }
}
