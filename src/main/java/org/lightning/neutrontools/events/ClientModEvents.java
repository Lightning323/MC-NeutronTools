package org.lightning.neutrontools.events;

import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;

import java.util.concurrent.atomic.AtomicInteger;


@EventBusSubscriber(
        modid = NeutronTools.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = net.neoforged.api.distmarker.Dist.CLIENT
)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
    }

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
                CreativeTabConfig.INSTANCE.load();
                //WE ARE READY! ============================================================
            }
        }
    }
//
//    @SubscribeEvent
//    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
//            event.accept(Items.DIAMOND);
//        }
//        if (event.getTabKey() == NeutronTools.EXAMPLE_TAB.getKey()) {
//            event.accept(Items.EMERALD);
//        }
//    }

    @SubscribeEvent
    public static void onClientLogin(PlayerEvent.PlayerLoggedInEvent event) {
        NeutronCreativeTabs.playerLoggedIn();
    }
}
