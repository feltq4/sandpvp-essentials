package com.sandpvp.gui;

import com.sandpvp.Config;
import com.sandpvp.HookStatus;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

/** Reset everything, and an honest status list of which render hooks are really installed. */
final class SettingsScreen extends BaseScreen {
    private boolean confirmReset = false;

    SettingsScreen(Screen parent) {
        super(parent, "Settings");
    }

    @Override
    protected void init() {
        int y = 4;
        header(y, Ui.orange("Settings"));
        y += 24;

        this.addRenderableWidget(Button.builder(
                Ui.light(confirmReset ? "Tap again to reset EVERYTHING" : "Reset all settings"), btn -> {
                    if (confirmReset) {
                        Config.resetAll();
                        confirmReset = false;
                        this.rebuildWidgets();
                    } else {
                        confirmReset = true;
                        btn.setMessage(Ui.light("Tap again to reset EVERYTHING"));
                    }
                }).bounds(left(), y, rowWidth(), 22).build());
        y += 26;

        header(y, Ui.light("Hook status (ACTIVE = working)"));
        y += 22;
        for (String feature : HookStatus.FEATURES) {
            this.addRenderableWidget(Button.builder(
                    Ui.light(feature + ": " + HookStatus.get(feature)), btn -> { })
                    .bounds(left(), y, rowWidth(), 16).build());
            y += 17;
        }
        y += 6;
        this.addRenderableWidget(Button.builder(Ui.light("Back"), btn -> back())
                .bounds(left(), y, rowWidth(), 22).build());
    }
}
