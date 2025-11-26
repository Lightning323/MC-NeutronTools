package me.Masonhades.hungerattribute.event;

import me.Masonhades.hungerattribute.HungerAttributeMod;
import me.Masonhades.hungerattribute.attribute.ModAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = HungerAttributeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HungerDataHandler {
    private static final String HUNGER_KEY = "hunger_multiplier_saved";

    public static void save(Player player, double value){
        player.getPersistentData().putDouble(HUNGER_KEY, value);
    }
    public static double load(Player player){
        if (player.getPersistentData().contains(HUNGER_KEY)){
            return player.getPersistentData().getDouble(HUNGER_KEY);
        }
        return 1.0;
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event){

        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player clone = event.getEntity();

        if (original.getPersistentData().contains(HUNGER_KEY)){
            double saved = HungerDataHandler.load(original);
            HungerDataHandler.save(clone, saved);
            Objects.requireNonNull(clone.getAttribute(ModAttributes.HUNGER_MULTIPLIER.get()))
                    .setBaseValue(saved);
        }
    }
}
