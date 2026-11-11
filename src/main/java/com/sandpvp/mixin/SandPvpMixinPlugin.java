package com.sandpvp.mixin;

import com.sandpvp.HookStatus;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Installs the render hooks into Minecraft after Mixin has applied the marker mixins.
 * It must not reference any Minecraft class directly (that would load it too early).
 * If a method cannot be found, the feature is simply reported as "NO MATCH" in the Settings screen;
 * the game never crashes because of it.
 */
public final class SandPvpMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        try {
            if (mixinClassName.endsWith(".HandMixin")) {
                int n = AsmTransformer.wrap(targetClass, m -> m.name.contains("ArmWithItem"),
                        AsmTransformer.POSE, AsmTransformer.ITEM, "handBegin", "handEnd");
                HookStatus.record(HookStatus.HAND, targetClassName, n);
            } else if (mixinClassName.endsWith(".FireMixin")) {
                int n = AsmTransformer.wrap(targetClass, m -> m.name.contains("Fire"),
                        AsmTransformer.POSE, null, "fireBegin", "fireEnd");
                HookStatus.record(HookStatus.FIRE, targetClassName, n);
            } else if (mixinClassName.endsWith(".TotemMixin")) {
                int n = AsmTransformer.wrap(targetClass, m -> m.name.contains("ItemActivation"),
                        AsmTransformer.GFX, null, "totemBegin", "totemEnd");
                HookStatus.record(HookStatus.TOTEM, targetClassName, n);
            } else if (mixinClassName.endsWith(".ParticleMixin")) {
                int n = AsmTransformer.guard(targetClass, m -> m.name.toLowerCase().contains("article"),
                        AsmTransformer.PARTICLE_OPTIONS, "hideParticle", true);
                HookStatus.record(HookStatus.PARTICLES, targetClassName, n);
            } else if (mixinClassName.endsWith(".CrosshairMixin")) {
                int n = AsmTransformer.guard(targetClass, m -> m.name.contains("rosshair"),
                        AsmTransformer.GFX, "hideVanillaCrosshair", false);
                HookStatus.record(HookStatus.CROSSHAIR, targetClassName, n);
            }
        } catch (Throwable t) {
            System.err.println("[SandPvP] Hook install failed for " + targetClassName + ": " + t);
        }
    }
}
