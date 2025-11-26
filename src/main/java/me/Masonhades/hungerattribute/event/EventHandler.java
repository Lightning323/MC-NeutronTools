package me.Masonhades.hungerattribute.event;


import me.Masonhades.hungerattribute.HungerAttributeMod;
import me.Masonhades.hungerattribute.attribute.ModAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = HungerAttributeMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandler {

    @SubscribeEvent
    public static void addAttributesToEntities(EntityAttributeModificationEvent event){
        event.add(EntityType.PLAYER, ModAttributes.HUNGER_MULTIPLIER.get());
    }
}
