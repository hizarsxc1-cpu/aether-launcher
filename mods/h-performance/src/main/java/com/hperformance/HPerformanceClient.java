package com.hperformance;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class HPerformanceClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static boolean enabled;
    private static Integer oldViewDistance;
    private static Integer oldSimulationDistance;
    private static Double oldEntityDistance;
    private static ParticlesMode oldParticles;
    private static CloudRenderMode oldClouds;
    private static Boolean oldEntityShadows;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hperformance.toggle", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P, "category.hperformance"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) toggle(client);
        });
    }

    private static void toggle(MinecraftClient client) {
        if (client.options == null) return;
        if (!enabled) apply(client);
        else restore(client);
    }

    private static void apply(MinecraftClient client) {
        var options = client.options;
        oldViewDistance = options.getViewDistance().getValue();
        oldSimulationDistance = options.getSimulationDistance().getValue();
        oldEntityDistance = options.getEntityDistanceScaling().getValue();
        oldParticles = options.getParticles().getValue();
        oldClouds = options.getCloudRenderMode().getValue();
        oldEntityShadows = options.getEntityShadows().getValue();

        options.getViewDistance().setValue(Math.min(oldViewDistance, 5));
        options.getSimulationDistance().setValue(Math.min(oldSimulationDistance, 4));
        options.getEntityDistanceScaling().setValue(Math.min(oldEntityDistance, 0.5));
        options.getParticles().setValue(ParticlesMode.MINIMAL);
        options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        options.getEntityShadows().setValue(false);
        enabled = true;
        notify(client, "POTATO mode ON - render lebih ringan");
    }

    private static void restore(MinecraftClient client) {
        var options = client.options;
        if (oldViewDistance != null) options.getViewDistance().setValue(oldViewDistance);
        if (oldSimulationDistance != null) options.getSimulationDistance().setValue(oldSimulationDistance);
        if (oldEntityDistance != null) options.getEntityDistanceScaling().setValue(oldEntityDistance);
        if (oldParticles != null) options.getParticles().setValue(oldParticles);
        if (oldClouds != null) options.getCloudRenderMode().setValue(oldClouds);
        if (oldEntityShadows != null) options.getEntityShadows().setValue(oldEntityShadows);
        enabled = false;
        notify(client, "POTATO mode OFF - pengaturan lama dipulihkan");
    }

    private static void notify(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[H Performance] " + message), true);
        }
    }
}
