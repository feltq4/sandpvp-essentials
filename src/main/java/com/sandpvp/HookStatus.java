package com.sandpvp;

import java.util.HashMap;
import java.util.Map;

/**
 * Remembers which render hooks were really installed into Minecraft's classes.
 * The Settings screen shows this, so you always know what is active.
 * This class must not touch any Minecraft class (it is used very early during startup).
 */
public final class HookStatus {
    public static final String HAND = "Handheld/Shield";
    public static final String FIRE = "Fire overlay";
    public static final String TOTEM = "Totem pop";
    public static final String PARTICLES = "Particles";
    public static final String CROSSHAIR = "Crosshair";
    public static final String[] FEATURES = { HAND, FIRE, TOTEM, PARTICLES, CROSSHAIR };

    private static final Map<String, Integer> HOOKED = new HashMap<>();
    private static final Map<String, Integer> SEEN = new HashMap<>();

    private HookStatus() {}

    public static synchronized void record(String feature, String targetClass, int hooks) {
        SEEN.merge(feature, 1, Integer::sum);
        HOOKED.merge(feature, hooks, Integer::sum);
        System.out.println("[SandPvP] " + feature + ": class " + targetClass + " -> " + hooks + " hook(s) installed");
    }

    public static synchronized String get(String feature) {
        if (HOOKED.getOrDefault(feature, 0) > 0) {
            return "ACTIVE";
        }
        if (SEEN.getOrDefault(feature, 0) > 0) {
            return "NO MATCH";
        }
        return "waiting";
    }
}
