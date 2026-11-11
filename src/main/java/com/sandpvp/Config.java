package com.sandpvp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Every setting of the mod. Saved to config/sandpvp.json. */
public final class Config {
    // Must stay the first field: settings register themselves here while they are created.
    private static final Map<String, Setting> ALL = new LinkedHashMap<>();

    public abstract static class Setting {
        public final String key;
        public final String label;

        Setting(String key, String label) {
            this.key = key;
            this.label = label;
            ALL.put(key, this);
        }

        public abstract void reset();
    }

    public static final class BoolSetting extends Setting {
        public boolean value;
        public final boolean def;

        BoolSetting(String key, String label, boolean def) {
            super(key, label);
            this.def = def;
            this.value = def;
        }

        @Override
        public void reset() {
            value = def;
        }
    }

    public static final class IntSetting extends Setting {
        public int value;
        public final int def;
        public final int min;
        public final int max;
        public final int step;

        IntSetting(String key, String label, int def, int min, int max, int step) {
            super(key, label);
            this.def = def;
            this.value = def;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        public void set(int v) {
            value = Math.max(min, Math.min(max, v));
        }

        @Override
        public void reset() {
            value = def;
        }

        public double toRatio() {
            return max == min ? 0.0 : (value - min) / (double) (max - min);
        }

        public int fromRatio(double ratio) {
            double steps = Math.round(ratio * (max - min) / (double) step);
            return min + (int) (steps * step);
        }

        /** How much the small - and + buttons change the value. */
        public int nudge() {
            return Math.max(step, (max - min) / 100);
        }
    }

    private static BoolSetting bool(String key, String label, boolean def) {
        return new BoolSetting(key, label, def);
    }

    private static IntSetting num(String key, String label, int def, int min, int max, int step) {
        return new IntSetting(key, label, def, min, max, step);
    }

    // ---- A. Small Pop (totem animation). X/Y in GUI pixels, size in percent.
    public static final BoolSetting TOTEM_ON = bool("totem.on", "Small Pop", false);
    public static final IntSetting TOTEM_X = num("totem.x", "X offset", 0, -300, 300, 1);
    public static final IntSetting TOTEM_Y = num("totem.y", "Y offset", 0, -300, 300, 1);
    public static final IntSetting TOTEM_SIZE = num("totem.size", "Size %", 60, 10, 300, 5);

    // ---- B. Low / Off fire. X/Y in 1/100 block units, size in percent.
    public static final BoolSetting FIRE_ON = bool("fire.on", "Low Fire", false);
    public static final BoolSetting FIRE_HIDE = bool("fire.hide", "Hide fire completely", false);
    public static final IntSetting FIRE_X = num("fire.x", "X offset", 0, -100, 100, 1);
    public static final IntSetting FIRE_Y = num("fire.y", "Y offset (down is minus)", -30, -100, 100, 1);
    public static final IntSetting FIRE_SIZE = num("fire.size", "Size %", 100, 10, 200, 5);

    // ---- C. Small handheld.
    public static final BoolSetting HAND_ON = bool("hand.on", "Small Handheld", false);
    public static final IntSetting HAND_X = num("hand.x", "X offset", 0, -100, 100, 1);
    public static final IntSetting HAND_Y = num("hand.y", "Y offset", 0, -100, 100, 1);
    public static final IntSetting HAND_SIZE = num("hand.size", "Size %", 80, 10, 200, 5);

    // ---- D. Low shield (only the picture, never the protection).
    public static final BoolSetting SHIELD_ON = bool("shield.on", "Low Shield", false);
    public static final BoolSetting SHIELD_HIDE = bool("shield.hide", "Hide shield while blocking", false);
    public static final IntSetting SHIELD_X = num("shield.x", "X offset", 0, -100, 100, 1);
    public static final IntSetting SHIELD_Y = num("shield.y", "Y offset", 0, -100, 100, 1);
    public static final IntSetting SHIELD_SIZE = num("shield.size", "Size %", 70, 10, 200, 5);

    // ---- E. Particle filter on/off (the per particle list is saved separately).
    public static final BoolSetting PARTICLES_ON = bool("particles.on", "Particle filter", true);

    // ---- F. Custom crosshair
    public static final BoolSetting CROSS_ON = bool("cross.on", "Custom Crosshair", false);
    public static final IntSetting CROSS_SIZE = num("cross.size", "Arm length", 5, 1, 30, 1);
    public static final IntSetting CROSS_THICK = num("cross.thick", "Thickness", 1, 1, 8, 1);
    public static final IntSetting CROSS_GAP = num("cross.gap", "Gap", 3, 0, 20, 1);
    public static final IntSetting CROSS_X = num("cross.x", "X offset", 0, -200, 200, 1);
    public static final IntSetting CROSS_Y = num("cross.y", "Y offset", 0, -200, 200, 1);
    public static final IntSetting CROSS_R = num("cross.r", "Color red", 255, 0, 255, 1);
    public static final IntSetting CROSS_G = num("cross.g", "Color green", 154, 0, 255, 1);
    public static final IntSetting CROSS_B = num("cross.b", "Color blue", 46, 0, 255, 1);
    public static final BoolSetting CROSS_OUTLINE = bool("cross.outline", "Outline", true);

    // ---- G. Armor HUD
    public static final BoolSetting ARMOR_ON = bool("armor.on", "Armor HUD", false);
    public static final IntSetting ARMOR_X = num("armor.x", "X position", 6, 0, 800, 1);
    public static final IntSetting ARMOR_Y = num("armor.y", "Y position", 100, 0, 500, 1);
    public static final IntSetting ARMOR_SCALE = num("armor.scale", "Scale %", 100, 50, 300, 5);

    // ---- H. Potion HUD
    public static final BoolSetting POTION_ON = bool("potion.on", "Potion HUD", false);
    public static final IntSetting POTION_X = num("potion.x", "X position", 6, 0, 800, 1);
    public static final IntSetting POTION_Y = num("potion.y", "Y position", 40, 0, 500, 1);
    public static final IntSetting POTION_SCALE = num("potion.scale", "Scale %", 100, 50, 300, 5);

    // ---- I. FPS and ping
    public static final BoolSetting FPS_ON = bool("fps.on", "FPS & Ping", false);
    public static final BoolSetting FPS_PING = bool("fps.ping", "Show ping", true);
    public static final IntSetting FPS_X = num("fps.x", "X position", 6, 0, 800, 1);
    public static final IntSetting FPS_Y = num("fps.y", "Y position", 6, 0, 500, 1);
    public static final IntSetting FPS_SIZE = num("fps.size", "Size %", 100, 50, 300, 5);
    public static final IntSetting FPS_R = num("fps.r", "Color red", 255, 0, 255, 1);
    public static final IntSetting FPS_G = num("fps.g", "Color green", 154, 0, 255, 1);
    public static final IntSetting FPS_B = num("fps.b", "Color blue", 46, 0, 255, 1);

    /** Ids of particles the user switched OFF (hidden). Empty = vanilla behaviour. */
    public static final Set<String> hiddenParticles = ConcurrentHashMap.newKeySet();
    /** Bumped whenever the particle list changes, so the fast lookup cache is rebuilt. */
    public static volatile int particleVersion = 0;

    public record ParticleEntry(String id, String name) {}

    private static List<ParticleEntry> particleCache;

    /** Every particle type registered in the game right now (vanilla and modded), read from the registry. */
    public static synchronized List<ParticleEntry> particles() {
        if (particleCache == null) {
            List<ParticleEntry> list = new ArrayList<>();
            for (var key : BuiltInRegistries.PARTICLE_TYPE.keySet()) {
                String ns = key.getNamespace();
                String name = pretty(key.getPath());
                if (!"minecraft".equals(ns)) {
                    name = name + " [" + ns + "]";
                }
                list.add(new ParticleEntry(key.toString(), name));
            }
            list.sort(Comparator.comparing(e -> e.name().toLowerCase()));
            particleCache = list;
        }
        return particleCache;
    }

    private static String pretty(String path) {
        StringBuilder sb = new StringBuilder();
        for (String part : path.split("[_/]")) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    public static void resetAll() {
        for (Setting s : ALL.values()) {
            s.reset();
        }
        hiddenParticles.clear();
        particleVersion++;
    }

    // ---- saving and loading

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Data {
        Map<String, Boolean> flags = new LinkedHashMap<>();
        Map<String, Double> numbers = new LinkedHashMap<>();
        List<String> hiddenParticles = new ArrayList<>();
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("sandpvp.json");
    }

    public static void load() {
        Path p = file();
        if (!Files.exists(p)) {
            return;
        }
        try {
            Data d = GSON.fromJson(Files.readString(p), Data.class);
            if (d == null) {
                return;
            }
            for (Setting s : ALL.values()) {
                if (s instanceof BoolSetting b && d.flags != null && d.flags.containsKey(s.key)) {
                    b.value = d.flags.get(s.key);
                } else if (s instanceof IntSetting n && d.numbers != null && d.numbers.containsKey(s.key)) {
                    n.set((int) Math.round(d.numbers.get(s.key)));
                }
            }
            hiddenParticles.clear();
            if (d.hiddenParticles != null) {
                hiddenParticles.addAll(d.hiddenParticles);
            }
            particleVersion++;
        } catch (Exception e) {
            System.err.println("[SandPvP] Could not read config, using defaults: " + e);
        }
    }

    public static void save() {
        Data d = new Data();
        for (Setting s : ALL.values()) {
            if (s instanceof BoolSetting b) {
                d.flags.put(s.key, b.value);
            } else if (s instanceof IntSetting n) {
                d.numbers.put(s.key, (double) n.value);
            }
        }
        d.hiddenParticles = new ArrayList<>(hiddenParticles);
        d.hiddenParticles.sort(String::compareTo);
        try {
            Path p = file();
            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(d));
        } catch (IOException e) {
            System.err.println("[SandPvP] Could not save config: " + e);
        }
    }

    private Config() {}
}
