package com.telekineticblades;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public final class TelekineticBladesClient implements ClientModInitializer {
    private static int basicCd, strikeCd, stormCd, judgmentCd, tick;

    @Override
    public void onInitializeClient() {
        // Video controls: left = Basic, right = Physic, Shift+right = Blade Storm,
        // Shift+left = Penghakiman. Vanilla left-click remains the Basic Attack.
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient || hand != net.minecraft.util.Hand.MAIN_HAND) return ActionResult.PASS;
            MinecraftClient c = MinecraftClient.getInstance();
            if (player.isSneaking()) {
                if (judgmentCd == 0) judgment(c);
                return ActionResult.FAIL;
            }
            if (entity instanceof LivingEntity living && basicCd == 0) {
                basicEffect(c, living);
                basicCd = 5;
            }
            return ActionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient && hand == net.minecraft.util.Hand.MAIN_HAND && player.isSneaking()) {
                if (judgmentCd == 0) judgment(MinecraftClient.getInstance());
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient || hand != net.minecraft.util.Hand.MAIN_HAND) return ActionResult.PASS;
            MinecraftClient c = MinecraftClient.getInstance();
            if (player.isSneaking()) {
                if (stormCd == 0) bladeStorm(c);
            } else if (strikeCd == 0) {
                physicStrike(c);
            }
            return ActionResult.FAIL;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!world.isClient || hand != net.minecraft.util.Hand.MAIN_HAND) return ActionResult.PASS;
            MinecraftClient c = MinecraftClient.getInstance();
            if (player.isSneaking()) {
                if (stormCd == 0) bladeStorm(c);
            } else if (strikeCd == 0) {
                physicStrike(c);
            }
            return ActionResult.FAIL;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient || hand != net.minecraft.util.Hand.MAIN_HAND)
                return TypedActionResult.pass(player.getStackInHand(hand));
            MinecraftClient c = MinecraftClient.getInstance();
            if (player.isSneaking()) {
                if (stormCd == 0) bladeStorm(c);
            } else if (strikeCd == 0) {
                physicStrike(c);
            }
            return TypedActionResult.fail(player.getStackInHand(hand));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            tick++;
            if (basicCd > 0) basicCd--;
            if (strikeCd > 0) strikeCd--;
            if (stormCd > 0) stormCd--;
            if (judgmentCd > 0) judgmentCd--;
            if (tick % 3 == 0) orbitBlades(client);
        });
    }

    private static List<LivingEntity> targets(MinecraftClient c, double range, double cone) {
        PlayerEntity p = c.player;
        if (p == null || c.world == null) return List.of();
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

    private static void basicEffect(MinecraftClient c, LivingEntity e) {
        if (c.world == null) return;
        Vec3d p = e.getBoundingBox().getCenter();
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI * 2 / 12.0;
            c.world.addParticle(ParticleTypes.CRIT, p.x + Math.cos(a) * 0.65,
                    p.y + 0.2, p.z + Math.sin(a) * 0.65, 0, 0.05, 0);
        }
    }

    private static void physicStrike(MinecraftClient c) {
        LivingEntity e = target(c, 14.0, 0.10);
        if (e == null) { message(c, "Arahkan crosshair ke musuh untuk Physic Strike"); return; }
        Vec3d center = e.getBoundingBox().getCenter();
        for (int i = 0; i < 36; i++) {
            double a = i * Math.PI * 2 / 36.0;
            c.world.addParticle(ParticleTypes.END_ROD, center.x + Math.cos(a) * 1.6,
                    center.y + 0.15, center.z + Math.sin(a) * 1.6, 0, 0.05, 0);
        }
        hit(c, e, 1);
        burst(c, e, 36, true);
        strikeCd = 28;
    }

    private static void bladeStorm(MinecraftClient c) {
        List<LivingEntity> list = targets(c, 18.0, -0.20).stream().limit(5).toList();
        if (list.isEmpty()) { message(c, "Arahkan crosshair ke area musuh untuk Blade Storm"); return; }
        for (LivingEntity e : list) {
            Vec3d p = e.getBoundingBox().getCenter();
            // Repeated descending blade trails around each selected target.
            for (int sword = 0; sword < 8; sword++) {
                double a = sword * Math.PI * 2 / 8.0;
                double x = p.x + Math.cos(a) * 2.0;
                double z = p.z + Math.sin(a) * 2.0;
                for (int j = 0; j < 12; j++) {
                    c.world.addParticle(ParticleTypes.END_ROD, x, p.y + 3.0 - j * 0.22, z,
                            -Math.cos(a) * 0.03, -0.08, -Math.sin(a) * 0.03);
                }
                hit(c, e, 1);
            }
            burst(c, e, 36, false);
        }
        stormCd = 90;
    }

    private static void judgment(MinecraftClient c) {
        List<LivingEntity> list = targets(c, 20.0, -0.30).stream().limit(8).toList();
        if (list.isEmpty()) { message(c, "Arahkan crosshair ke musuh untuk Penghakiman"); return; }
        for (LivingEntity e : list) {
            Vec3d p = e.getBoundingBox().getCenter();
            // Expanding rings and a large impact to represent the ultimate.
            for (int ring = 0; ring < 4; ring++) {
                for (int i = 0; i < 40; i++) {
                    double a = i * Math.PI * 2 / 40.0;
                    c.world.addParticle(ParticleTypes.END_ROD,
                            p.x + Math.cos(a) * (1.0 + ring * 0.85),
                            p.y + 0.4 + ring * 0.45,
                            p.z + Math.sin(a) * (1.0 + ring * 0.85),
                            Math.cos(a) * 0.04, 0.02, Math.sin(a) * 0.04);
                }
            }
            hit(c, e, 2);
            burst(c, e, 64, true);
        }
        judgmentCd = 220;
    }

    private static void hit(MinecraftClient c, LivingEntity e, int count) {
        if (c.interactionManager == null || c.player == null) return;
        for (int i = 0; i < count && e.isAlive(); i++) {
            c.interactionManager.attackEntity(c.player, e);
            c.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        }
    }

    private static void burst(MinecraftClient c, LivingEntity e, int amount, boolean heavy) {
        Vec3d p = e.getBoundingBox().getCenter();
        for (int i = 0; i < amount; i++) {
            double a = i * Math.PI * 2 / amount;
            double r = heavy ? 2.0 : 1.25;
            c.world.addParticle(ParticleTypes.END_ROD, p.x + Math.cos(a) * r,
                    p.y + (i % 7) * 0.3, p.z + Math.sin(a) * r,
                    Math.cos(a) * 0.09, 0.05, Math.sin(a) * 0.09);
        }
        if (heavy) {
            c.world.addParticle(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 0, 0, 0);
            c.world.addParticle(ParticleTypes.FLASH, p.x, p.y + 0.5, p.z, 0, 0, 0);
        } else c.world.addParticle(ParticleTypes.CRIT, p.x, p.y, p.z, 0, 0.2, 0);
    }

    private static void orbitBlades(MinecraftClient c) {
        if (c.player == null || c.world == null) return;
        Vec3d p = c.player.getPos().add(0, 1.25, 0);
        for (int i = 0; i < 4; i++) {
            double a = (c.player.age * 0.12) + i * Math.PI / 2;
            c.world.addParticle(ParticleTypes.END_ROD, p.x + Math.cos(a) * 1.1,
                    p.y + Math.sin(a * 2) * 0.25, p.z + Math.sin(a) * 1.1, 0, 0.01, 0);
        }
    }

    private static void message(MinecraftClient c, String s) {
        if (c.player != null) c.player.sendMessage(Text.literal("[Telekinetic Blades] " + s), true);
    }
}
