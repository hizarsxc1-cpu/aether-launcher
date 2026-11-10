package com.hfastanchor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public final class HFastAnchorClient implements ClientModInitializer {
    private static KeyBinding anchorKey;

    private enum Stage { IDLE, WAIT_ANCHOR, WAIT_CHARGE }
    private static Stage stage = Stage.IDLE;

    private static BlockPos anchorPos;
    private static int glowstoneSlot = -1;
    private static int ticks;
    private static long stageStartWorldTime;

    // Java inventory indices: 0=hotbar slot 1, 8=hotbar slot 9.
    private static final int OBSIDIAN_SLOT = 1;
    private static final int CRYSTAL_SLOT = 2;
    private static final int GLOWSTONE_SLOT = 7;
    private static final int ANCHOR_SLOT = 8;

    @Override
    public void onInitializeClient() {
        anchorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hfastanchor.combo", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z, "category.hfastanchor"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.interactionManager == null) {
                reset();
                return;
            }

            while (anchorKey.wasPressed()) {
                if (stage == Stage.IDLE) startAnchor(client);
            }
            // Crystal-only fast place: Obsidian remains completely manual.
            // A tap sends one placement attempt; holding sends two rapid attempts per tick.
            boolean useTapped = client.options.useKey.wasPressed();
            boolean useHeld = client.options.useKey.isPressed();
            if ((useTapped || useHeld) && stage == Stage.IDLE) {
                runCrystal(client, useHeld);
            }
            // If the crosshair is on an End Crystal while attacking, keep/select
            // the Crystal in hand. Never switch to a sword for Crystal breaking.
            if (client.options.attackKey.isPressed()
                    && client.crosshairTarget instanceof EntityHitResult entityHit
                    && entityHit.getEntity() instanceof EndCrystalEntity
                    && client.player.getInventory().getStack(CRYSTAL_SLOT).isOf(Items.END_CRYSTAL)) {
                select(client, CRYSTAL_SLOT);
            }
            tickAnchor(client);
        });
    }

    private static void startAnchor(MinecraftClient client) {
        int anchor = requireHotbarItem(client, ANCHOR_SLOT, Items.RESPAWN_ANCHOR);
        glowstoneSlot = requireHotbarItem(client, GLOWSTONE_SLOT, Items.GLOWSTONE);
        if (anchor < 0 || glowstoneSlot < 0) {
            reset();
            return;
        }

        // Prefer the normal crosshair block. If an entity is in front of it,
        // raycast the world directly so the entity does not steal the target.
        BlockHitResult hit;
        if (client.crosshairTarget instanceof BlockHitResult crosshairHit
                && crosshairHit.getType() == HitResult.Type.BLOCK) {
            hit = crosshairHit;
        } else {
            HitResult ray = client.player.raycast(4.5, 1.0F, false);
            if (!(ray instanceof BlockHitResult blockHit)
                    || ray.getType() != HitResult.Type.BLOCK) {
                reset();
                return;
            }
            hit = blockHit;
        }

        // The adjacent block can be fire left on top of a support block.
        // It is replaceable, so the anchor can occupy that exact position.
        BlockPos clickedPos = hit.getBlockPos();
        BlockPos adjacentPos = clickedPos.offset(hit.getSide());
        anchorPos = client.world.getBlockState(clickedPos).isReplaceable()
                ? clickedPos : adjacentPos;
        if (!client.world.getBlockState(anchorPos).isReplaceable()) {
            reset();
            return;
        }

        select(client, anchor);
        ActionResult result = client.interactionManager.interactBlock(
                client.player, Hand.MAIN_HAND, hit);
        if (result.isAccepted()) {
            stage = Stage.WAIT_ANCHOR;
            ticks = 0;
            stageStartWorldTime = client.world.getTime();
        } else {
            reset();
        }
    }

    private static void tickAnchor(MinecraftClient client) {
        if (stage == Stage.IDLE) return;
        if (client.currentScreen != null) {
            reset();
            return;
        }
        if (anchorPos == null) {
            reset();
            return;
        }

        BlockState state = client.world.getBlockState(anchorPos);
        boolean exists = state.isOf(Blocks.RESPAWN_ANCHOR);

        switch (stage) {
            case WAIT_ANCHOR -> {
                if (client.world.getTime() <= stageStartWorldTime) return;
                // Give the server one tick to acknowledge placement, then
                // send the charge interaction without waiting for a long block update.
                if (exists || ++ticks >= 2) {
                    select(client, glowstoneSlot);
                    interactAtAnchor(client);
                    stage = Stage.WAIT_CHARGE;
                    ticks = 0;
                    stageStartWorldTime = client.world.getTime();
                }
            }
            case WAIT_CHARGE -> {
                if (client.world.getTime() <= stageStartWorldTime) return;
                if (client.world.getRegistryKey() == World.NETHER) {
                    reset();
                    return;
                }

                if (exists && state.get(RespawnAnchorBlock.CHARGES) > 0) {
                    // Switch away from Glowstone and trigger the explosion immediately.
                    selectActivationItem(client);
                    interactAtAnchor(client);
                    client.player.swingHand(Hand.MAIN_HAND);
                    reset();
                } else if (++ticks <= 4) {
                    // Retry quickly while the server/client block state catches up.
                    select(client, glowstoneSlot);
                    interactAtAnchor(client);
                } else {
                    reset();
                }
            }
            default -> { }
        }
    }

    private static void selectActivationItem(MinecraftClient client) {
        PlayerInventory inv = client.player.getInventory();

        // First choice: a Totem in the hotbar.
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                select(client, i);
                return;
            }
        }

        // Prefer an empty slot, then Obsidian, then Crystal, then another safe item.
        for (int i = 1; i < 9; i++) {
            if (i != GLOWSTONE_SLOT && i != ANCHOR_SLOT && inv.getStack(i).isEmpty()) {
                select(client, i);
                return;
            }
        }
        if (inv.getStack(OBSIDIAN_SLOT).isOf(Items.OBSIDIAN)) {
            select(client, OBSIDIAN_SLOT);
            return;
        }
        if (inv.getStack(CRYSTAL_SLOT).isOf(Items.END_CRYSTAL)) {
            select(client, CRYSTAL_SLOT);
            return;
        }
        for (int i = 1; i < 7; i++) {
            if (!inv.getStack(i).isOf(Items.GLOWSTONE)
                    && !inv.getStack(i).isOf(Items.RESPAWN_ANCHOR)) {
                select(client, i);
                return;
            }
        }
    }

    private static void runCrystal(MinecraftClient client, boolean holdingUse) {
        // Only place a Crystal on an existing Obsidian/Bedrock block.
        // Never select or place Obsidian here: that remains the user's normal input.
        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos supportPos = hit.getBlockPos();
        BlockState support = client.world.getBlockState(supportPos);
        if (!support.isOf(Blocks.OBSIDIAN) && !support.isOf(Blocks.BEDROCK)) return;

        PlayerInventory inv = client.player.getInventory();
        if (!inv.getStack(CRYSTAL_SLOT).isOf(Items.END_CRYSTAL)) return;

        BlockPos crystalPos = supportPos.up();
        if (!client.world.getBlockState(crystalPos).isAir()
                || !client.world.getBlockState(crystalPos.up()).isAir()) return;

        // Keep Crystal selected after placement so the next attack breaks it
        // with Crystal in hand instead of visibly swapping to a sword.
        select(client, CRYSTAL_SLOT);

        // Increase held-input burst to three attempts per client tick.
        // Server acceptance and real CPS still depend on tick rate and latency.
        int attempts = holdingUse ? 3 : 1;
        for (int i = 0; i < attempts; i++) {
            ActionResult result = client.interactionManager.interactBlock(
                    client.player, Hand.MAIN_HAND, hit);
            if (result.isAccepted()) {
                client.player.swingHand(Hand.MAIN_HAND);
            }
        }

        // Intentionally do not restore the old slot: Crystal remains in hand.
    }

    private static int requireHotbarItem(MinecraftClient client, int slot, Item item) {
        return client.player.getInventory().getStack(slot).isOf(item) ? slot : -1;
    }

    private static void interactAtAnchor(MinecraftClient client) {
        BlockHitResult hit = new BlockHitResult(
                anchorPos.toCenterPos(), Direction.UP, anchorPos, false);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
    }

    private static void select(MinecraftClient client, int slot) {
        // interactBlock() synchronizes the selected hotbar slot internally.
        client.player.getInventory().selectedSlot = slot;
    }

    private static void reset() {
        stage = Stage.IDLE;
        anchorPos = null;
        glowstoneSlot = -1;
        ticks = 0;
        stageStartWorldTime = 0L;
    }
}
