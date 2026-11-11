package com.telekineticblades;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public final class TelekineticBladesClient implements ClientModInitializer {
    private static KeyBinding basic, strike, storm, judgment;
    private static int basicCd, strikeCd, stormCd, judgmentCd, tick;

    @Override
    public void onInitializeClient() {
        basic = key("Basic Attack", GLFW.GLFW_KEY_Q);
        strike = key("Physic Strike", GLFW.GLFW_KEY_E);
        storm = key("Blade Storm", GLFW.GLFW_KEY_R);
        judgment = key("Penghakiman", GLFW.GLFW_KEY_G);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.interactionManager == null) return;
            tick++;
            if (basicCd > 0) basicCd--;
            if (strikeCd > 0) strikeCd--;
            if (stormCd > 0) stormCd--;
            if (judgmentCd > 0) judgmentCd--;
            if (basic.wasPressed() && basicCd == 0) basicAttack(client);
            if (strike.wasPressed() && strikeCd == 0) physicStrike(client);
            if (storm.wasPressed() && stormCd == 0) bladeStorm(client);
            if (judgment.wasPressed() && judgmentCd == 0) judgment(client);
            if (tick % 3 == 0) orbitBlades(client);
        });
    }

    private static KeyBinding key(String name, int code) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.telekinetic_blades." + name.toLowerCase().replace(' ', '_'),
                InputUtil.Type.KEYSYM, code, "category.telekinetic_blades"));
    }

    private static List<LivingEntity> targets(MinecraftClient c, double range, double cone) {
        PlayerEntity p = c.player;
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Box area = p.getBoundingBox().expand(range);
        return c.world.getEntitiesByClass(LivingEntity.class, area,
                e -> e.isAlive() && e != p && !e.isSpectator()
                        && p.squaredDistanceTo(e) <= range * range
                        && e.getBoundingBox().getCenter().subtract(p.getEyePos()).normalize().dotProduct(look) >= cone)
                .stream().sorted(Comparator.comparingDouble(p::squaredDistanceTo)).toList();
    }

    private static LivingEntity target(MinecraftClient c, double range, double cone) {
        List<LivingEntity> list = targets(c, range, cone);
        return list.isEmpty() ? null : list.get(0);
    }

    private static void basicAttack(MinecraftClient c) {
        LivingEntity e = target(c, 5.0, 0.45);
        if (e == null) { message(c, "Basic Attack: target tidak ditemukan"); return; }
        hit(c, e, 1);
        burst(c, e, 12, false);
        basicCd = 8;
    }

    private static void physicStrike(MinecraftClient c) {
        LivingEntity e = target(c, 12.0, 0.25);
        if (e == null) { message(c, "Physic Strike: target tidak ditemukan"); return; }
        Vec3d center = e.getBoundingBox().getCenter();
        for (int i = 0; i < 28; i++) {
            double a = i * Math.PI * 2 / 28.0;
            c.world.addParticle(ParticleTypes.END_ROD, center.x + Math.cos(a) * 1.5,
                    center.y + 0.2, center.z + Math.sin(a) * 1.5, 0, 0.04, 0);
        }
        hit(c, e, 1);
        burst(c, e, 30, true);
        strikeCd = 35;
    }

    private static void bladeStorm(MinecraftClient c) {
        List<LivingEntity> list = targets(c, 15.0, -0.15).stream().limit(5).toList();
        if (list.isEmpty()) { message(c, "Blade Storm: tidak ada musuh"); return; }
        for (LivingEntity e : list) {
            for (int n = 0; n < 3; n++) {
                Vec3d pos = e.getBoundingBox().getCenter().add((n - 1) * 0.65, 2.5 + n * 0.35, 0);
                for (int i = 0; i < 7; i++) {
                    c.world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y - i * 0.28, pos.z,
                            0, -0.03, 0);
                }
                hit(c, e, 1);
            }
            burst(c, e, 24, false);
        }
        stormCd = 100;
    }

    private static void judgment(MinecraftClient c) {
        List<LivingEntity> list = targets(c, 18.0, -0.25).stream().limit(8).toList();
        if (list.isEmpty()) { message(c, "Penghakiman: tidak ada musuh"); return; }
        for (LivingEntity e : list) {
            Vec3d p = e.getBoundingBox().getCenter();
            for (int ring = 0; ring < 3; ring++) {
                for (int i = 0; i < 32; i++) {
                    double a = i * Math.PI * 2 / 32.0;
                    c.world.addParticle(ParticleTypes.FLASH, p.x + Math.cos(a) * (1 + ring),
                            p.y + ring * 0.7, p.z + Math.sin(a) * (1 + ring), 0, 0, 0);
                }
            }
            hit(c, e, 1);
            burst(c, e, 48, true);
        }
        judgmentCd = 240;
    }

    private static void hit(MinecraftClient c, LivingEntity e, int count) {
        for (int i = 0; i < count; i++) {
            if (!e.isAlive()) break;
            c.interactionManager.attackEntity(c.player, e);
            c.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        }
    }

    private static void burst(MinecraftClient c, LivingEntity e, int amount, boolean heavy) {
        Vec3d p = e.getBoundingBox().getCenter();
        for (int i = 0; i < amount; i++) {
            double a = i * Math.PI * 2 / amount;
            double r = heavy ? 1.7 : 1.0;
            c.world.addParticle(ParticleTypes.END_ROD, p.x + Math.cos(a) * r,
                    p.y + (i % 5) * 0.35, p.z + Math.sin(a) * r,
                    Math.cos(a) * 0.08, 0.04, Math.sin(a) * 0.08);
        }
        if (heavy) c.world.addParticle(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 0, 0, 0);
        else c.world.addParticle(ParticleTypes.CRIT, p.x, p.y, p.z, 0, 0.2, 0);
    }

    private static void orbitBlades(MinecraftClient c) {
        Vec3d p = c.player.getPos().add(0, 1.25, 0);
        for (int i = 0; i < 4; i++) {
            double a = (c.player.age * 0.12) + i * Math.PI / 2;
            c.world.addParticle(ParticleTypes.END_ROD, p.x + Math.cos(a) * 1.1,
                    p.y + Math.sin(a * 2) * 0.25, p.z + Math.sin(a) * 1.1, 0, 0.01, 0);
        }
    }

    private static void message(MinecraftClient c, String s) {
        c.player.sendMessage(Text.literal("[Telekinetic Blades] " + s), true);
    }
}
