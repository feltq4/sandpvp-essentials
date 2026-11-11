package com.sandpvp.mixin;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Marks the Minecraft class(es) that draw the vanilla crosshair drawing.
 * Mixin applies this marker, then SandPvpMixinPlugin#postApply installs the real hooks
 * by looking up the methods by name and parameter type (so a renamed method cannot crash the game).
 * Classes that do not exist in this Minecraft version are skipped with a log line.
 */
@Mixin(targets = { "net.minecraft.client.gui.Gui", "net.minecraft.client.gui.Hud" })
public abstract class CrosshairMixin {
}
