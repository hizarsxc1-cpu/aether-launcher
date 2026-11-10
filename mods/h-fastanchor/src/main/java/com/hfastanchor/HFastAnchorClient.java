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
    private static KeyBinding crystalKey;

    private enum Stage { IDLE, WAIT_ANCHOR, WAIT_CHARGE, WAIT_CONFIRM }
    private static Stage stage = Stage.IDLE;

    private static BlockPos anchorPos;
    private static int glowstoneSlot = -1;
    private static int ticks;

    // Java inventory indices: 0=hotbar slot 1, 8=hotbar slot 9.
    private static final int SWORD_SLOT = 0;
    private static final int OBSIDIAN_SLOT = 1;
    private static final int CRYSTAL_SLOT = 2;
    private static final int GLOWSTONE_SLOT = 7;
    private static final int ANCHOR_SLOT = 8;

    @Override
    public void onInitializeClient() {
        anchorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hfastanchor.combo", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z, "category.hfastanchor"));
        crystalKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hfastanchor.crystal", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X, "category.hfastanchor"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.interactionManager == null) {
                reset();
                return;
            }

            while (anchorKey.wasPressed()) {
                if (stage == Stage.IDLE) startAnchor(client);
            }
            while (crystalKey.wasPressed()) {
                if (stage == Stage.IDLE) runCrystal(client);
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

        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            reset();
            return;
        }

        anchorPos = hit.getBlockPos().offset(hit.getSide());
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
        } else {
            reset();
        }
    }

    private static void tickAnchor(MinecraftClient client) {
        if (stage == Stage.IDLE) return;
        if (client.currentScreen != null || anchorPos == null) {
            reset();
            return;
        }

        BlockState state = client.world.getBlockState(anchorPos);
        boolean exists = state.isOf(Blocks.RESPAWN_ANCHOR);

        switch (stage) {
            case WAIT_ANCHOR -> {
                if (exists) {
                    select(client, glowstoneSlot);
                    interactAtAnchor(client);
                    stage = Stage.WAIT_CHARGE;
                    ticks = 0;
                } else if (++ticks > 30) {
                    reset();
                }
            }
            case WAIT_CHARGE -> {
                if (!exists) {
                    reset();
                    return;
                }
                if (state.get(RespawnAnchorBlock.CHARGES) > 0) {
                    // Never leave Glowstone in hand for the activation click.
                    // Prefer a Totem; otherwise use Obsidian/Crystal/empty slot.
                    selectActivationItem(client);
                    interactAtAnchor(client);
                    client.player.swingHand(Hand.MAIN_HAND);
                    stage = Stage.WAIT_CONFIRM;
                    ticks = 0;
                } else if (++ticks > 30) {
                    reset();
                } else if (ticks % 2 == 0) {
                    select(client, glowstoneSlot);
                    interactAtAnchor(client);
                }
            }
            case WAIT_CONFIRM -> {
                // Respawn Anchors do not explode in the Nether.
                if (client.world.getRegistryKey() == World.NETHER) {
                    reset();
                    return;
                }
                if (!exists || ++ticks > 12) {
                    reset();
                } else if (ticks % 2 == 0) {
                    interactAtAnchor(client);
                    client.player.swingHand(Hand.MAIN_HAND);
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

        // Do not select the user's sword (slot 1). Prefer an empty slot,
        // then Obsidian, then Crystal, then any other safe hotbar item.
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

    private static void runCrystal(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) return;

        PlayerInventory inv = client.player.getInventory();
        if (!inv.getStack(OBSIDIAN_SLOT).isOf(Items.OBSIDIAN)
                || !inv.getStack(CRYSTAL_SLOT).isOf(Items.END_CRYSTAL)) return;

        BlockPos pos = hit.getBlockPos().offset(hit.getSide());
        if (!client.world.getBlockState(pos).isReplaceable()) return;
        if (!client.world.getBlockState(pos.up()).isAir()
                || !client.world.getBlockState(pos.up(2)).isAir()) return;

        // Send the Obsidian placement first, then the Crystal placement
        // immediately in the same client tick. The server processes these
        // interaction packets in order; no block-update wait is added.
        select(client, OBSIDIAN_SLOT);
        ActionResult obsidianResult = client.interactionManager.interactBlock(
                client.player, Hand.MAIN_HAND, hit);
        if (!obsidianResult.isAccepted()) return;

        BlockHitResult top = new BlockHitResult(
                Vec3d.ofCenter(pos).add(0.0, 0.5, 0.0),
                Direction.UP, pos, false);
        select(client, CRYSTAL_SLOT);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, top);
        client.player.swingHand(Hand.MAIN_HAND);
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
        client.player.getInventory().selectedSlot = slot;
        client.interactionManager.syncSelectedSlot();
    }

    private static void reset() {
        stage = Stage.IDLE;
        anchorPos = null;
        glowstoneSlot = -1;
        ticks = 0;
    }
}
