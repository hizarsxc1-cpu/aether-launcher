package com.hfastanchor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class HFastAnchorClient implements ClientModInitializer {
    private static KeyBinding anchorKey;
    private static KeyBinding crystalKey;

    private enum Stage { IDLE, WAIT_ANCHOR, WAIT_CHARGE, WAIT_CONFIRM }
    private static Stage stage = Stage.IDLE;
    private static BlockPos anchorPos;
    private static int anchorSlot = -1, glowstoneSlot = -1;
    private static int ticks;

    private static final int SWORD_SLOT = 0;
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
                if (stage != Stage.IDLE) message(client, "Anchor combo masih berjalan.");
                else startAnchor(client);
            }
            while (crystalKey.wasPressed()) runCrystal(client);
            tickAnchor(client);
        });
    }

    private static void startAnchor(MinecraftClient client) {
        anchorSlot = requireHotbarItem(client, ANCHOR_HOTBAR_SLOT, Items.RESPAWN_ANCHOR, "Respawn Anchor harus di slot 9.");
        glowstoneSlot = requireHotbarItem(client, GLOWSTONE_HOTBAR_SLOT, Items.GLOWSTONE, "Glowstone harus di slot 8.");
        if (anchorSlot < 0 || glowstoneSlot < 0) {
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
            message(client, "Anchor ditempatkan; menunggu konfirmasi blok dari game/server.");
        } else {
            message(client, "Pemasangan Anchor ditolak. Cek target dan slot 9.");
            reset();
        }
    }

    private static void tickAnchor(MinecraftClient client) {
        if (stage == Stage.IDLE) return;
        if (client.currentScreen != null) {
            message(client, "Macro dihentikan karena inventory/menu terbuka.");
            reset();
            return;
        }
        ticks++;
        if (ticks > 40) {
            message(client, "Server tidak mengonfirmasi rangkaian. Cek ping, target, atau proteksi server.");
            reset();
            return;
        }

        BlockState state = client.world.getBlockState(anchorPos);
        boolean anchorExists = state.isOf(net.minecraft.block.Blocks.RESPAWN_ANCHOR);

        switch (stage) {
            case WAIT_ANCHOR -> {
                if (anchorExists) {
                    select(client, glowstoneSlot);
                    interactAtAnchor(client);
                    stage = Stage.WAIT_CHARGE;
                    ticks = 0;
                }
            }
            case WAIT_CHARGE -> {
                if (anchorExists && state.get(RespawnAnchorBlock.CHARGES) > 0) {
                    equipTotemIfAvailableOrSword(client);
                    // Detonate as soon as the charged state is observed; no fixed tick delay.
                    interactAtAnchor(client);
                    client.player.swingHand(Hand.MAIN_HAND);
                    stage = Stage.WAIT_CONFIRM;
                    ticks = 0;
                }
            }
            case WAIT_CONFIRM -> {
                if (!anchorExists) {
                    message(client, "Anchor meledak!");
                    reset();
                }
            }
            default -> { }
        }
    }

    private static void equipTotemIfAvailableOrSword(MinecraftClient client) {
        PlayerInventory inv = client.player.getInventory();

        // Keep an existing offhand Totem; otherwise swap one from hotbar or inventory.
        if (inv.offHand.get(0).isOf(Items.TOTEM_OF_UNDYING)) {
            select(client, SWORD_SLOT);
            message(client, "Totem sudah siap di offhand.");
            return;
        }

        for (int i = 0; i < 9; i++) {
            if (!inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) continue;
            // PlayerScreenHandler hotbar slots are 36..44; SWAP button 40 targets offhand.
            client.interactionManager.clickSlot(
                    client.player.currentScreenHandler.syncId, 36 + i, 40,
                    SlotActionType.SWAP, client.player);
            select(client, SWORD_SLOT);
            message(client, "Totem dari hotbar dipindahkan ke offhand.");
            return;
        }

        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isOf(Items.TOTEM_OF_UNDYING)) continue;
            // PlayerScreenHandler main inventory slots are 9..35; button 40 swaps offhand.
            client.interactionManager.clickSlot(
                    client.player.currentScreenHandler.syncId, i, 40,
                    SlotActionType.SWAP, client.player);
            select(client, SWORD_SLOT);
            message(client, "Totem dari inventory dipindahkan ke offhand.");
            return;
        }

        // No Totem? Do not abort. Use Sword in main hand and still try the explosion.
        select(client, SWORD_SLOT);
        message(client, "Totem tidak ada; detonasi tetap dijalankan.");
    }

    private static void runCrystal(MinecraftClient client) {
        if (stage != Stage.IDLE) {
            message(client, "Tunggu Anchor combo selesai sebelum Crystal.");
            return;
        }
        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            message(client, "Arahkan crosshair ke sisi blok untuk menempatkan Obsidian.");
            return;
        }
        if (!client.player.getInventory().getStack(OBSIDIAN_SLOT).isOf(Items.OBSIDIAN)) {
            message(client, "Obsidian harus ada di slot 2.");
            return;
        }
        if (!client.player.getInventory().getStack(CRYSTAL_SLOT).isOf(Items.END_CRYSTAL)) {
            message(client, "End Crystal harus ada di slot 3.");
            return;
        }

        BlockPos obsidianPos = hit.getBlockPos().offset(hit.getSide());
        BlockHitResult top = new BlockHitResult(
                Vec3d.ofCenter(obsidianPos).add(0.0, 0.49, 0.0),
                Direction.UP, obsidianPos, false);

        // X: place Obsidian (slot 2), then End Crystal (slot 3), no artificial delay.
        select(client, OBSIDIAN_SLOT);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
        select(client, CRYSTAL_SLOT);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, top);
        client.player.swingHand(Hand.MAIN_HAND);
        message(client, "Crystal sequence dikirim; hancurkan Crystal secara manual.");
    }

    private static int requireHotbarItem(MinecraftClient client, int slot, Item item, String error) {
        if (client.player.getInventory().getStack(slot).isOf(item)) return slot;
        message(client, error);
        return -1;
    }

    private static void interactAtAnchor(MinecraftClient client) {
        BlockHitResult hit = new BlockHitResult(
                anchorPos.toCenterPos(), Direction.UP, anchorPos, false);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
    }

    private static void select(MinecraftClient client, int slot) {
        client.player.getInventory().selectedSlot = slot;
    }

    private static void message(MinecraftClient client, String text) {
        if (client.player != null) client.player.sendMessage(Text.literal("[AETHER Macro] " + text), true);
    }

    private static void reset() {
        stage = Stage.IDLE;
        anchorPos = null;
        anchorSlot = glowstoneSlot = -1;
        ticks = 0;
    }
}
