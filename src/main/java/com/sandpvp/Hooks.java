package com.sandpvp;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Called from the bytecode that SandPvpMixinPlugin writes into Minecraft's render classes.
 * Everything here only changes how things are DRAWN on this client. No gameplay is touched.
 * The method names and descriptors are referenced from AsmTransformer, do not rename them.
 */
public final class Hooks {
    private static final float TINY = 0.0001f;
    private static final ConcurrentHashMap<Object, Boolean> PARTICLE_CACHE = new ConcurrentHashMap<>();
    private static int cacheVersion = -1;

    private Hooks() {}

    private static void applyView(PoseStack pose, Config.IntSetting x, Config.IntSetting y, Config.IntSetting size) {
        pose.translate(x.value / 100f, y.value / 100f, 0f);
        float s = size.value / 100f;
        pose.scale(s, s, s);
    }

    // ---- first person items and shield

    public static void handBegin(PoseStack pose, ItemStack stack) {
        pose.pushPose();
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            boolean shield = stack != null && stack.is(Items.SHIELD);
            if (shield && Config.SHIELD_ON.value && player != null && player.isBlocking()) {
                if (Config.SHIELD_HIDE.value) {
                    pose.scale(TINY, TINY, TINY);
                } else {
                    applyView(pose, Config.SHIELD_X, Config.SHIELD_Y, Config.SHIELD_SIZE);
                }
            } else if (Config.HAND_ON.value) {
                applyView(pose, Config.HAND_X, Config.HAND_Y, Config.HAND_SIZE);
            }
        } catch (Throwable ignored) {
            // never break rendering
        }
    }

    public static void handEnd(PoseStack pose) {
        pose.popPose();
    }

    // ---- first person fire overlay

    public static void fireBegin(PoseStack pose) {
        pose.pushPose();
        try {
            if (!Config.FIRE_ON.value) {
                return;
            }
            if (Config.FIRE_HIDE.value) {
                pose.scale(TINY, TINY, TINY);
            } else {
                applyView(pose, Config.FIRE_X, Config.FIRE_Y, Config.FIRE_SIZE);
            }
        } catch (Throwable ignored) {
            // never break rendering
        }
    }

    public static void fireEnd(PoseStack pose) {
        pose.popPose();
    }

    // ---- totem of undying animation

    public static void totemBegin(GuiGraphicsExtractor graphics) {
        Matrix3x2fStack m = graphics.pose();
        m.pushMatrix();
        try {
            if (!Config.TOTEM_ON.value) {
                return;
            }
            var window = Minecraft.getInstance().getWindow();
            float cx = window.getGuiScaledWidth() / 2f;
            float cy = window.getGuiScaledHeight() / 2f;
            float s = Config.TOTEM_SIZE.value / 100f;
            m.translate(cx + Config.TOTEM_X.value, cy + Config.TOTEM_Y.value);
            m.scale(s, s);
            m.translate(-cx, -cy);
        } catch (Throwable ignored) {
            // never break rendering
        }
    }

    public static void totemEnd(GuiGraphicsExtractor graphics) {
        graphics.pose().popMatrix();
    }

    // ---- particles (true = do not create this particle on this client)

    public static boolean hideParticle(ParticleOptions options) {
        try {
            if (options == null || !Config.PARTICLES_ON.value || Config.hiddenParticles.isEmpty()) {
                return false;
            }
            int version = Config.particleVersion;
            if (cacheVersion != version) {
                PARTICLE_CACHE.clear();
                cacheVersion = version;
            }
            Object type = options.getType();
            Boolean hidden = PARTICLE_CACHE.get(type);
            if (hidden == null) {
                Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType());
                hidden = id != null && Config.hiddenParticles.contains(id.toString());
                PARTICLE_CACHE.put(type, hidden);
            }
            return hidden;
        } catch (Throwable t) {
            return false;
        }
    }

    // ---- vanilla crosshair (true = skip it, because the custom one is on)

    public static boolean hideVanillaCrosshair() {
        return Config.CROSS_ON.value;
    }
}
