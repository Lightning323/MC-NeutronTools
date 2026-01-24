package org.zipcoder.neutrontools.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.creativetabs.CreativeTabs;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabEdits;

import java.util.concurrent.atomic.AtomicInteger;


@Mod.EventBusSubscriber(modid = NeutronTools.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CreativeTabReadyEventHandler {

    private static boolean tagsReady = false;
    private static AtomicInteger initialIndexedTabs = new AtomicInteger(0);

    public static boolean isTagsReady() {
        return tagsReady;
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        // This check ensures we only update the creative tabs when Item tags are updated,
        // since we need them in order to determine which items go in which tabs
        if (event.getRegistryAccess().registry(Registries.ITEM).isPresent()) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                tagsReady = true;
                reloadIfReady();
            }
        }
    }

    public static void onCreativeTabReady(CreativeModeTab self) {
        if (CreativeTabEdits.INSTANCE.original_SortedTabs.contains(self)) {
            initialIndexedTabs.getAndIncrement();
            if (FMLEnvironment.dist == Dist.CLIENT) {
                reloadIfReady();
            }
        }
    }

    private static void reloadIfReady() {
        if (initialIndexedTabs.get() >= CreativeTabEdits.INSTANCE.original_SortedTabs.size() && isTagsReady() && !CreativeTabEdits.INSTANCE.isWasReloadedFirstTime()) {
            //IT IS CRUCIAL that we dont reload tabs before this point in order to properly index the original state of the tabs
            NeutronTools.LOGGER.info("Indexed {}/{} original tabs and item tags are ready. Reloading creative tabs!",
                    initialIndexedTabs,
                    CreativeTabEdits.INSTANCE.original_SortedTabs.size());
            CreativeTabs.reloadTabs();
        }
    }
}
