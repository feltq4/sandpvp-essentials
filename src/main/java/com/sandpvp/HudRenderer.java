package com.sandpvp;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.Collection;

/** Crosshair, armor HUD, potion HUD and FPS/ping display. All drawn with simple rectangles and text. */
public final class HudRenderer {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
    private static final String[] LABELS = { "Helmet", "Chest", "Legs", "Boots" };
    private static final int LIGHT_ORANGE = 0xFFFFC48A;

    private static long lastNanos = System.nanoTime();
    private static int frames;
    private static int fps;

    private HudRenderer() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(SandPvpClient.MOD_ID, "hud"),
                HudRenderer::extract);
    }

    private static void extract(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        countFrame();
        if (Config.CROSS_ON.value && mc.options.getCameraType().isFirstPerson()) {
            drawCrosshair(g, mc);
        }
        if (Config.ARMOR_ON.value) {
            drawArmor(g, mc, player);
        }
        if (Config.POTION_ON.value) {
            drawPotions(g, mc, player);
        }
        if (Config.FPS_ON.value) {
            drawFps(g, mc, player);
        }
    }

    private static void countFrame() {
        frames++;
        long now = System.nanoTime();
        if (now - lastNanos >= 1_000_000_000L) {
            fps = frames;
            frames = 0;
            lastNanos = now;
        }
    }

    // ---- crosshair

    private static void drawCrosshair(GuiGraphicsExtractor g, Minecraft mc) {
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int cx = w / 2 + Config.CROSS_X.value;
        int cy = h / 2 + Config.CROSS_Y.value;
        int len = Config.CROSS_SIZE.value;
        int t = Config.CROSS_THICK.value;
        int gap = Config.CROSS_GAP.value;
        int color = 0xFF000000 | (Config.CROSS_R.value << 16) | (Config.CROSS_G.value << 8) | Config.CROSS_B.value;
        int half = t / 2;
        int top = cy - half;
        int left = cx - half;

        int[][] arms = {
                { cx - gap - len, top, cx - gap, top + t },          // left
                { cx + gap, top, cx + gap + len, top + t },                  // right
                { left, cy - gap - len, left + t, cy - gap },        // up
                { left, cy + gap, left + t, cy + gap + len }                // down
        };
        if (Config.CROSS_OUTLINE.value) {
            for (int[] a : arms) {
                g.fill(a[0] - 1, a[1] - 1, a[2] + 1, a[3] + 1, 0xFF000000);
            }
        }
        for (int[] a : arms) {
            g.fill(a[0], a[1], a[2], a[3], color);
        }
    }

    // ---- armor durability

    private static void drawArmor(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player) {
        Font font = mc.font;
        Matrix3x2fStack m = g.pose();
        float s = Config.ARMOR_SCALE.value / 100f;
        m.pushMatrix();
        m.translate(Config.ARMOR_X.value, Config.ARMOR_Y.value);
        m.scale(s, s);
        int y = 0;
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty()) {
                continue;
            }
            g.text(font, LABELS[i], 0, y, LIGHT_ORANGE, true);
            if (stack.isDamageableItem()) {
                int max = Math.max(1, stack.getMaxDamage());
                int pct = Math.max(0, Math.min(100, (max - stack.getDamageValue()) * 100 / max));
                int color = 0xFF000000 | healthColor(pct);
                g.fill(38, y + 1, 88, y + 8, 0xAA000000);
                g.fill(38, y + 1, 38 + pct / 2, y + 8, color);
                g.text(font, pct + "%", 92, y, color, true);
            } else {
                g.text(font, "--", 38, y, 0xFFAAAAAA, true);
            }
            y += 11;
        }
        m.popMatrix();
    }

    private static int healthColor(int pct) {
        int r = pct < 50 ? 255 : (int) (255 * (100 - pct) / 50.0);
        int gr = pct > 50 ? 255 : (int) (255 * pct / 50.0);
        return (r << 16) | (gr << 8) | 40;
    }

    // ---- potion effects

    private static void drawPotions(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player) {
        Collection<MobEffectInstance> effects = player.getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }
        Font font = mc.font;
        Matrix3x2fStack m = g.pose();
        float s = Config.POTION_SCALE.value / 100f;
        m.pushMatrix();
        m.translate(Config.POTION_X.value, Config.POTION_Y.value);
        m.scale(s, s);
        int y = 0;
        for (MobEffectInstance effect : effects) {
            Identifier id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
            if (id != null) {
                Identifier sprite = Identifier.fromNamespaceAndPath(id.getNamespace(), "mob_effect/" + id.getPath());
                g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, y, 16, 16);
            }
            String name = effect.getEffect().value().getDisplayName().getString();
            if (effect.getAmplifier() > 0) {
                name = name + " " + (effect.getAmplifier() + 1);
            }
            g.text(font, name, 20, y, LIGHT_ORANGE, true);
            g.text(font, formatTime(effect), 20, y + 8, 0xFFFFFFFF, true);
            y += 20;
        }
        m.popMatrix();
    }

    private static String formatTime(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) {
            return "infinite";
        }
        int seconds = effect.getDuration() / 20;
        return (seconds / 60) + ":" + (seconds % 60 < 10 ? "0" : "") + (seconds % 60);
    }

    // ---- FPS and ping

    private static void drawFps(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player) {
        String text = "FPS: " + fps;
        if (Config.FPS_PING.value) {
            ClientPacketListener connection = mc.getConnection();
            PlayerInfo info = connection == null ? null : connection.getPlayerInfo(player.getUUID());
            text = text + "  Ping: " + (info == null ? "--" : info.getLatency() + " ms");
        }
        int color = 0xFF000000 | (Config.FPS_R.value << 16) | (Config.FPS_G.value << 8) | Config.FPS_B.value;
        Matrix3x2fStack m = g.pose();
        float s = Config.FPS_SIZE.value / 100f;
        m.pushMatrix();
        m.translate(Config.FPS_X.value, Config.FPS_Y.value);
        m.scale(s, s);
        g.text(mc.font, text, 0, 0, color, true);
        m.popMatrix();
    }
}
