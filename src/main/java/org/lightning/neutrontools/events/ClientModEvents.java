package org.lightning.neutrontools.events;

import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.lightning.neutrontools.config.creativeTabs.TabEditConfig;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;
import java.util.concurrent.atomic.AtomicInteger;


@EventBusSubscriber(
        modid = NeutronTools.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = net.neoforged.api.distmarker.Dist.CLIENT
)
public class ClientModEvents {

//    @SubscribeEvent
//    public static void onClientSetup(FMLClientSetupEvent event) {
//    }

//    private static boolean tagsReady = false;
//
//    @SubscribeEvent
//    public static void onTagsUpdated(TagsUpdatedEvent event) {
//        // This check ensures we only update the creative tabs when Item tags are updated,
//        // since we need them in order to determine which items go in which tabs
//        if (event.getRegistryAccess().registry(Registries.ITEM).isPresent()) {
//            if (FMLEnvironment.dist == Dist.CLIENT) {
//                tagsReady = true;
////                CreativeTabConfig.INSTANCE.load();
//            }
//        }
//    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        //TODO: Some mods change the way creative tabs are registered, and so the only reliable way to ensure modded items are added is to add them here, instead of in the mixin
        //Putting them here also prevents weird crashes
        TabEditConfig tabEditConfig = CreativeTabConfig.INSTANCE.tabEdits.get(event.getTab());
        if (tabEditConfig != null) {
//            event.insertAfter(I); //TODO: Luckly we can still insert items at specific positions
            tabEditConfig.modifyContentsFromEvent(event);
        }
    }

//    @SubscribeEvent
//    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
//    }
}
