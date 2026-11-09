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

    private enum Stage { IDLE, WAIT_ANCHOR, WAIT_CHARGE, WAIT_CONFIRM, WAIT_OBSIDIAN }
    private static Stage stage = Stage.IDLE;

    private static BlockPos anchorPos;
    private static BlockPos crystalObsidianPos;
    private static int glowstoneSlot = -1;
    private static int crystalTicks;

    private static final int OBSIDIAN_SLOT = 1;
    private static final int CRYSTAL_SLOT = 2;
    private static final int GLOWSTONE_HOTBAR_SLOT = 7;
    private static final int ANCHOR_HOTBAR_SLOT = 8;

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
            tickMacro(client);
        });
    }

    private static void startAnchor(MinecraftClient client) {
        int anchorSlot = requireHotbarItem(client, ANCHOR_HOTBAR_SLOT, Items.RESPAWN_ANCHOR);
        glowstoneSlot = requireHotbarItem(client, GLOWSTONE_HOTBAR_SLOT, Items.GLOWSTONE);
        if (anchorSlot < 0 || glowstoneSlot < 0) {
            reset();
            return;
        }

        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            reset();
            return;
        }

        anchorPos = hit.getBlockPos().offset(hit.getSide());
        BlockState targetState = client.world.getBlockState(anchorPos);
        if (!targetState.isReplaceable()) {
            reset();
            return;
        }

        select(client, anchorSlot);
        ActionResult result = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
        if (result.isAccepted()) {
            stage = Stage.WAIT_ANCHOR;
            crystalTicks = 0;
        } else {
            reset();
        }
    }

    private static void tickMacro(MinecraftClient client) {
        if (stage == Stage.IDLE) return;
        if (client.currentScreen != null) {
            reset();
            return;
        }

        if (stage == Stage.WAIT_OBSIDIAN) {
            tickCrystal(client);
            return;
        }

        BlockState state = client.world.getBlockState(anchorPos);
        boolean anchorExists = state.isOf(Blocks.RESPAWN_ANCHOR);

        switch (stage) {
            case WAIT_ANCHOR -> {
                if (anchorExists) {
                    select(client, glowstoneSlot);
                    interactAtAnchor(client);
                    stage = Stage.WAIT_CHARGE;
                    crystalTicks = 0;
                } else if (++crystalTicks > 40) {
                    reset();
                }
            }
            case WAIT_CHARGE -> {
                if (anchorExists && state.get(RespawnAnchorBlock.CHARGES) > 0) {
                    // A Totem in the main hand protects the player and is consumed from its hotbar slot.
                    // If no hotbar Totem exists, leave Glowstone selected and still attempt detonation.
                    selectHotbarTotemOrKeepCurrent(client);
                    interactAtAnchor(client);
                    client.player.swingHand(Hand.MAIN_HAND);
                    stage = Stage.WAIT_CONFIRM;
                    crystalTicks = 0;
                } else if (++crystalTicks > 40) {
                    reset();
                }
            }
            case WAIT_CONFIRM -> {
                if (!anchorExists) {
                    reset();
                } else if (++crystalTicks > 40) {
                    reset();
                }
            }
            default -> { }
        }
    }

    private static void selectHotbarTotemOrKeepCurrent(MinecraftClient client) {
        PlayerInventory inv = client.player.getInventory();

        // Prefer a Totem from the hotbar (slot 1..9). Do not swap items into the offhand.
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                select(client, i);
                return;
            }
        }

        // An offhand Totem still protects the player while Glowstone remains in the main hand.
        // If neither hand has a Totem, the charged Anchor is still activated.
    }

    private static void runCrystal(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (!client.player.getInventory().getStack(OBSIDIAN_SLOT).isOf(Items.OBSIDIAN)) return;
        if (!client.player.getInventory().getStack(CRYSTAL_SLOT).isOf(Items.END_CRYSTAL)) return;

        crystalObsidianPos = hit.getBlockPos().offset(hit.getSide());
        if (!client.world.getBlockState(crystalObsidianPos).isReplaceable()) {
            crystalObsidianPos = null;
            return;
        }

        // Place Obsidian first, then wait only for the actual block-state update (no fixed delay).
        select(client, OBSIDIAN_SLOT);
        ActionResult result = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
        if (result.isAccepted()) {
            stage = Stage.WAIT_OBSIDIAN;
            crystalTicks = 0;
        } else {
            crystalObsidianPos = null;
        }
    }

    private static void tickCrystal(MinecraftClient client) {
        if (client.world.getBlockState(crystalObsidianPos).isOf(Blocks.OBSIDIAN)) {
            BlockPos above = crystalObsidianPos.up();
            BlockHitResult top = new BlockHitResult(
                    Vec3d.ofCenter(crystalObsidianPos).add(0.0, 0.5, 0.0),
                    Direction.UP, crystalObsidianPos, false);

            select(client, CRYSTAL_SLOT);
            client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, top);
            client.player.swingHand(Hand.MAIN_HAND);
            reset();
            return;
        }

        // Stop waiting if the server never accepts the Obsidian placement.
        if (++crystalTicks > 40) reset();
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
    }

    // Intentionally silent: no macro status text above the hotbar or in chat.
    private static void reset() {
        stage = Stage.IDLE;
        anchorPos = null;
        crystalObsidianPos = null;
        glowstoneSlot = -1;
        crystalTicks = 0;
    }
}
