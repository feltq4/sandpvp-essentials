package com.sandpvp.gui;

import com.sandpvp.Config;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

/** Main menu with the five categories. */
public final class MainScreen extends BaseScreen {
    public MainScreen() {
        super(null, "SandPvP Essentials");
    }

    @Override
    protected void init() {
        int rowH = 26;
        int y = Math.max(6, (this.height - 7 * rowH) / 2);
        header(y, Ui.orange("SandPvP Essentials"));
        y += rowH;
        y = add(y, "Visuals", this::visuals);
        y = add(y, "Particles", () -> new ParticleScreen(this));
        y = add(y, "HUD", this::hud);
        y = add(y, "Crosshair", () -> new ModuleScreen(this, "Crosshair", List.of(
                Config.CROSS_ON, Config.CROSS_SIZE, Config.CROSS_THICK, Config.CROSS_GAP,
                Config.CROSS_R, Config.CROSS_G, Config.CROSS_B, Config.CROSS_OUTLINE,
                Config.CROSS_X, Config.CROSS_Y)));
        y = add(y, "Settings", () -> new SettingsScreen(this));
        this.addRenderableWidget(Button.builder(Ui.light("Done"), btn -> this.onClose())
                .bounds(left(), y, rowWidth(), 22).build());
    }

    private int add(int y, String label, Supplier<Screen> target) {
        this.addRenderableWidget(Button.builder(Ui.light(label), btn -> open(target.get()))
                .bounds(left(), y, rowWidth(), 22).build());
        return y + 26;
    }

    private Screen visuals() {
        CategoryScreen s = new CategoryScreen(this, "Visuals");
        s.add("Small Pop (totem)", () -> new ModuleScreen(s, "Small Pop", List.of(
                Config.TOTEM_ON, Config.TOTEM_X, Config.TOTEM_Y, Config.TOTEM_SIZE)));
        s.add("Low / Off Fire", () -> new ModuleScreen(s, "Low / Off Fire", List.of(
                Config.FIRE_ON, Config.FIRE_HIDE, Config.FIRE_X, Config.FIRE_Y, Config.FIRE_SIZE)));
        s.add("Small Handheld", () -> new ModuleScreen(s, "Small Handheld", List.of(
                Config.HAND_ON, Config.HAND_X, Config.HAND_Y, Config.HAND_SIZE)));
        s.add("Low Shield", () -> new ModuleScreen(s, "Low Shield", List.of(
                Config.SHIELD_ON, Config.SHIELD_HIDE, Config.SHIELD_X, Config.SHIELD_Y, Config.SHIELD_SIZE)));
        return s;
    }

    private Screen hud() {
        CategoryScreen s = new CategoryScreen(this, "HUD");
        s.add("Armor HUD", () -> new ModuleScreen(s, "Armor HUD", List.of(
                Config.ARMOR_ON, Config.ARMOR_X, Config.ARMOR_Y, Config.ARMOR_SCALE)));
        s.add("Potion HUD", () -> new ModuleScreen(s, "Potion HUD", List.of(
                Config.POTION_ON, Config.POTION_X, Config.POTION_Y, Config.POTION_SCALE)));
        s.add("FPS & Ping", () -> new ModuleScreen(s, "FPS & Ping", List.of(
                Config.FPS_ON, Config.FPS_PING, Config.FPS_X, Config.FPS_Y, Config.FPS_SIZE,
                Config.FPS_R, Config.FPS_G, Config.FPS_B)));
        return s;
    }
}
