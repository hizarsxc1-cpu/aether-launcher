package dev.aether.touchmacro;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class AetherTouchMacroClient implements ClientModInitializer {
    private static KeyBinding anchorKey;
    private static KeyBinding crystalKey;

    private static final int SWORD_SLOT = 0;
    private static final int OBSIDIAN_SLOT = 1;
    private static final int CRYSTAL_SLOT = 2;
    private static final int GLOWSTONE_SLOT = 7;
    private static final int ANCHOR_SLOT = 8;

    @Override
    public void onInitializeClient() {
        anchorKey = register("key.aether_touch_macro.anchor", GLFW.GLFW_KEY_Z);
        crystalKey = register("key.aether_touch_macro.crystal", GLFW.GLFW_KEY_X);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (anchorKey.wasPressed()) runAnchor(client);
            while (crystalKey.wasPressed()) runCrystal(client);
        });
    }

    private static KeyBinding register(String translationKey, int keyCode) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey, InputUtil.Type.KEYSYM, keyCode, "category.aether_touch_macro"));
    }

    private static boolean ready(MinecraftClient client) {
        return client.player != null && client.world != null
                && client.interactionManager != null && client.currentScreen == null;
    }

    private static void runAnchor(MinecraftClient client) {
        if (!ready(client)) return;
        if (!(client.crosshairTarget instanceof BlockHitResult groundHit)) {
            message(client, "Arahkan crosshair ke sisi blok untuk menempatkan Anchor.");
            return;
        }

        BlockPos anchorPos = groundHit.getBlockPos().offset(groundHit.getSide());
        BlockHitResult anchorHit = topHit(anchorPos);

        // Z: Anchor -> Glowstone -> optional Totem to offhand -> detonate.
        // All interactions are sent in sequence without an intentional tick delay.
        select(client, ANCHOR_SLOT);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, groundHit);

        select(client, GLOWSTONE_SLOT);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, anchorHit);

        boolean hasTotem = moveAnyTotemToOffhand(client);
        // Sword is the main-hand item used to trigger the charged anchor.
        // If no Totem exists, the same detonation attempt still happens.
        select(client, SWORD_SLOT);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, anchorHit);
        client.player.swingHand(Hand.MAIN_HAND);

        message(client, hasTotem
                ? "Anchor sequence dikirim; Totem dipasang di offhand."
                : "Totem tidak ditemukan; detonasi tetap dicoba.");
    }

    private static void runCrystal(MinecraftClient client) {
        if (!ready(client)) return;
        if (!(client.crosshairTarget instanceof BlockHitResult groundHit)) {
            message(client, "Arahkan crosshair ke sisi blok untuk menempatkan Obsidian.");
            return;
        }

        BlockPos obsidianPos = groundHit.getBlockPos().offset(groundHit.getSide());
        BlockHitResult obsidianTop = topHit(obsidianPos);

        select(client, OBSIDIAN_SLOT);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, groundHit);

        select(client, CRYSTAL_SLOT);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, obsidianTop);
        client.player.swingHand(Hand.MAIN_HAND);
        message(client, "Crystal sequence dikirim. Hancurkan crystal secara manual.");
    }

    private static boolean moveAnyTotemToOffhand(MinecraftClient client) {
        PlayerInventory inventory = client.player.getInventory();
        if (inventory.offHand.get(0).isOf(Items.TOTEM_OF_UNDYING)) return true;

        // Scan all 36 main-inventory slots: hotbar 0..8 and storage 9..35.
        for (int inventoryIndex = 0; inventoryIndex < 36; inventoryIndex++) {
            if (!inventory.getStack(inventoryIndex).isOf(Items.TOTEM_OF_UNDYING)) continue;
            int screenSlot = inventoryIndex < 9 ? 36 + inventoryIndex : inventoryIndex;
            client.interactionManager.clickSlot(
                    client.player.currentScreenHandler.syncId,
                    screenSlot,
                    40,
                    SlotActionType.SWAP,
                    client.player
            );
            return true;
        }
        return false;
    }

    private static BlockHitResult topHit(BlockPos pos) {
        return new BlockHitResult(Vec3d.ofCenter(pos).add(0.0, 0.49, 0.0),
                Direction.UP, pos, false);
    }

    private static void select(MinecraftClient client, int slot) {
        client.player.getInventory().selectedSlot = slot;
    }

    private static void message(MinecraftClient client, String text) {
        client.player.sendMessage(Text.literal("AETHER: " + text), true);
    }
}
