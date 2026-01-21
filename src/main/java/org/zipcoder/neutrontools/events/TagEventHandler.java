package org.zipcoder.neutrontools.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.creativetabs.CreativeTabs;
import org.zipcoder.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;

@Mod.EventBusSubscriber(modid = NeutronTools.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TagEventHandler {

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        // This check ensures we only update the creative tabs when Item tags are updated,
        // since we need them in order to determine which items go in which tabs
        if (event.getRegistryAccess().registry(Registries.ITEM).isPresent()) {
            NeutronTools.LOGGER.info("Item tags have been registered and are now bound! Reloading creative tabs");
            CreativeTabs.reloadTabs();
        }
    }
}