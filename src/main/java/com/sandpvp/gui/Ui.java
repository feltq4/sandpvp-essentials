package com.sandpvp.gui;

import net.minecraft.network.chat.Component;

/** Orange and light-orange text helpers for the menu. */
public final class Ui {
    public static final int ORANGE = 0xFF9A2E;
    public static final int LIGHT = 0xFFC48A;
    public static final int GRAY = 0x9A9A9A;

    private Ui() {}

    public static Component orange(String text) {
        return Component.literal(text).withStyle(style -> style.withColor(ORANGE));
    }

    public static Component light(String text) {
        return Component.literal(text).withStyle(style -> style.withColor(LIGHT));
    }

    public static Component toggle(String label, boolean on) {
        return Component.literal(label + ": ")
                .withStyle(style -> style.withColor(LIGHT))
                .append(Component.literal(on ? "ON" : "OFF")
                        .withStyle(style -> style.withColor(on ? ORANGE : GRAY)));
    }
}
