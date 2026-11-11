package com.sandpvp.gui;

import com.sandpvp.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Shared layout helpers. Settings are saved to disk whenever a screen closes. */
abstract class BaseScreen extends Screen {
    protected final Screen parent;

    protected BaseScreen(Screen parent, String title) {
        super(Component.literal(title));
        this.parent = parent;
    }

    protected int rowWidth() {
        return Math.min(300, this.width - 16);
    }

    protected int left() {
        return (this.width - rowWidth()) / 2;
    }

    protected Button header(int y, Component text) {
        Button button = Button.builder(text, btn -> { })
                .bounds(left(), y, rowWidth(), 20).build();
        this.addRenderableWidget(button);
        return button;
    }

    protected void back() {
        if (parent == null) {
            this.onClose();
        } else {
            Minecraft.getInstance().gui.setScreen(parent);
        }
    }

    protected void open(Screen screen) {
        Minecraft.getInstance().gui.setScreen(screen);
    }

    @Override
    public void removed() {
        Config.save();
        super.removed();
    }
}
