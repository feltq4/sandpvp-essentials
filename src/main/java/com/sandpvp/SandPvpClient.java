package com.sandpvp;

import com.mojang.blaze3d.platform.InputConstants;
import com.sandpvp.gui.MainScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SandPvpClient implements ClientModInitializer {
    public static final String MOD_ID = "sandpvp";

    private static KeyMapping openGuiKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));

        // Shows up in Options > Controls > Key Binds under "SandPvP Essentials".
        openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.sandpvp.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                category));

        HudRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.consumeClick()) {
                if (client.player != null) {
                    client.gui.setScreen(new MainScreen());
                }
            }
        });
    }
}
