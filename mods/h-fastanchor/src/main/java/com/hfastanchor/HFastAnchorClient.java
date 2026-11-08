package com.hfastanchor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public final class HFastAnchorClient implements ClientModInitializer {
    private static KeyBinding comboKey;
    private enum Stage { IDLE, WAIT_ANCHOR, WAIT_CHARGE, WAIT_DETONATE, WAIT_CONFIRM }
    private static Stage stage = Stage.IDLE;
    private static BlockPos anchorPos;
    private static int anchorSlot = -1, glowstoneSlot = -1, totemSlot = -1;
    private static int ticks;
    private static int detonateAttempts;

    @Override
    public void onInitializeClient() {
        comboKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hfastanchor.combo", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V, "category.hfastanchor"));
        ClientTickEvents.END_CLIENT_TICK.register(HFastAnchorClient::tick);
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) {
            reset();
            return;
        }
        if (comboKey.wasPressed()) {
            if (stage != Stage.IDLE) message(client, "Combo masih berjalan.");
            else start(client);
        }
        if (stage == Stage.IDLE) return;
        ticks++;
        if (ticks > 40) {
            message(client, "Timeout menunggu update server. Combo dibatalkan.");
            reset();
            return;
        }

        BlockState state = client.world.getBlockState(anchorPos);
        boolean anchorStillThere = state.isOf(net.minecraft.block.Blocks.RESPAWN_ANCHOR);
        switch (stage) {
            case WAIT_ANCHOR -> {
                if (anchorStillThere) {
                    select(client, glowstoneSlot);
                    interactAtAnchor(client);
                    stage = Stage.WAIT_CHARGE;
                    ticks = 0;
                }
            }
            case WAIT_CHARGE -> {
                if (anchorStillThere && state.get(RespawnAnchorBlock.CHARGES) > 0) {
                    select(client, totemSlot);
                    stage = Stage.WAIT_DETONATE;
                    ticks = 0;
                    detonateAttempts = 0;
                }
            }
            case WAIT_DETONATE -> {
                if (!anchorStillThere) {
                    message(client, "Anchor berhasil meledak!");
                    reset();
                } else if (state.get(RespawnAnchorBlock.CHARGES) > 0 && ticks >= 3) {
                    // Re-select immediately before each attempt so the main hand is the Totem.
                    select(client, totemSlot);
                    interactAtAnchor(client);
                    detonateAttempts++;
                    ticks = 0;
                    if (detonateAttempts >= 3) {
                        stage = Stage.WAIT_CONFIRM;
                        ticks = 0;
                    }
                }
            }
            case WAIT_CONFIRM -> {
                if (!anchorStillThere) {
                    message(client, "Anchor berhasil meledak!");
                    reset();
                } else if (ticks >= 10) {
                    message(client, "Detonasi tidak terkonfirmasi. Cek dimensi, ping, atau proteksi server.");
                    reset();
                }
            }
            default -> { }
        }
    }

    private static void start(MinecraftClient client) {
        anchorSlot = findSlot(client, Items.RESPAWN_ANCHOR);
        glowstoneSlot = findSlot(client, Items.GLOWSTONE);
        totemSlot = findSlot(client, Items.TOTEM_OF_UNDYING);
        if (anchorSlot < 0 || glowstoneSlot < 0 || totemSlot < 0) {
            message(client, "Anchor, Glowstone, dan Totem harus ada di hotbar slot 1–9.");
            reset();
            return;
        }
        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            message(client, "Arahkan crosshair ke sisi blok tempat Anchor dipasang.");
            reset();
            return;
        }
        anchorPos = hit.getBlockPos().offset(hit.getSide());
        if (!client.world.getBlockState(anchorPos).isReplaceable()) {
            message(client, "Tempat pemasangan Anchor terhalang.");
            reset();
            return;
        }
        select(client, anchorSlot);
        ActionResult result = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
        if (result.isAccepted()) {
            stage = Stage.WAIT_ANCHOR;
            ticks = 0;
        } else {
            message(client, "Interaksi pemasangan Anchor ditolak client.");
            reset();
        }
    }

    private static void interactAtAnchor(MinecraftClient client) {
        BlockHitResult hit = new BlockHitResult(
                anchorPos.toCenterPos(), Direction.UP, anchorPos, false);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
    }

    private static int findSlot(MinecraftClient client, Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.isOf(item)) return i;
        }
        return -1;
    }

    private static void select(MinecraftClient client, int slot) {
        client.player.getInventory().selectedSlot = slot;
    }

    private static void message(MinecraftClient client, String text) {
        if (client.player != null) client.player.sendMessage(Text.literal("[H FastAnchor] " + text), true);
    }

    private static void reset() {
        stage = Stage.IDLE;
        anchorPos = null;
        anchorSlot = glowstoneSlot = totemSlot = -1;
        ticks = 0;
        detonateAttempts = 0;
    }
}
