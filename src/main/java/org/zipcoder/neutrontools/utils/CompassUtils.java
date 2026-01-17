package org.zipcoder.neutrontools.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CompassUtils {
    /**
     * Updates the compass in the player's main hand to point to the specified coordinates.
     *
     * @param player    The player holding the compass
     * @param targetPos The target position to point to
     * @param level     The current level/dimension
     * @return true if successful, false if player isn't holding a compass
     */
    public static boolean updateHeldCompass(Player player, BlockPos targetPos, Level level) {
        ItemStack heldItem = player.getMainHandItem();

        // Check if the held item is a compass
        if (heldItem.getItem() != Items.COMPASS) {
            return false;
        }
        System.out.println("COMPASS: " + level.dimension().location().toString());
// Create or update the compass NBT
        CompoundTag tag = heldItem.getOrCreateTag();
        CompoundTag lodestoneTag = new CompoundTag();
        lodestoneTag.putString("dimension", level.dimension().location().toString());
        lodestoneTag.putInt("posX", targetPos.getX());
        lodestoneTag.putInt("posY", targetPos.getY());
        lodestoneTag.putInt("posZ", targetPos.getZ());
        tag.put("LodestonePos", lodestoneTag);
        tag.putBoolean("LodestoneTracked", true);


        // Optional: Add a custom name if it doesn't have one
        if (!heldItem.hasCustomHoverName()) {
            heldItem.setHoverName(Component.literal("Waypoint Compass")
                    .withStyle(style -> style.withItalic(false).withColor(0x00AA00))); // Dark green
        }

        // Optional: Send a confirmation message
        player.displayClientMessage(
                Component.literal("Compass now points to ")
                        .append(Component.literal(String.format("[%d, %d, %d]",
                                        targetPos.getX(), targetPos.getY(), targetPos.getZ()))
                                .withStyle(style -> style.withColor(0x55FF55))), // Light green
                true
        );

        return true;
    }
}