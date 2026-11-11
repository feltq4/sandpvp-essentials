package com.sandpvp.gui;

import com.sandpvp.Config;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** Slider for one number setting. Changes apply immediately because the game reads the setting every frame. */
final class IntSlider extends AbstractSliderButton {
    private Config.IntSetting setting;

    IntSlider(int x, int y, int width, int height, Config.IntSetting setting) {
        super(x, y, width, height, Component.empty(), setting.toRatio());
        this.setting = setting;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        if (setting == null) {
            return;
        }
        this.setMessage(Ui.orange(setting.label + ": " + setting.value));
    }

    @Override
    protected void applyValue() {
        setting.set(setting.fromRatio(this.value));
        updateMessage();
    }

    /** Moves the slider knob after the value was changed by the - or + button. */
    void sync() {
        this.value = setting.toRatio();
        updateMessage();
    }
}
