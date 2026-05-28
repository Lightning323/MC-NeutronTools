package org.lightning.neutrontools.events;

import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.config.creativeTabs.TabEditConfig;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.core.registries.BuiltInRegistries;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;

@EventBusSubscriber(
        modid = NeutronTools.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = net.neoforged.api.distmarker.Dist.CLIENT
)
public class ClientModEvents {

    // Register this on the MinecraftForge.EVENT_BUS or use @SubscribeEvent
    @SubscribeEvent
    public static void onToastAdd(ToastAddEvent event) {
        if (event.getToast() instanceof RecipeToast && NeutronTools.CONFIG.hideRecipeToasts) {
            event.setCanceled(true);
        } else if (event.getToast() instanceof TutorialToast && NeutronTools.CONFIG.hideTutorialToasts) {
            event.setCanceled(true);
        }
    }

    //    @SubscribeEvent
//    public static void onClientSetup(FMLClientSetupEvent event) {
//    }

//    @SubscribeEvent
//    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
//    }

    private static boolean tagsReady = false;

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        // This check ensures we only update the creative tabs when Item tags are updated,
        // since we need them in order to determine which items go in which tabs
        if (event.getRegistryAccess().registry(Registries.ITEM).isPresent()) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                tagsReady = true;
                NeutronTools.CONFIG_DISABLED_ITEMS.loadItems();
            }
        }
    }

    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        //FIXME: It matters A GREAT DEAL where this is loaded, this seems to be the only reliable way to ensure modded items are added
        //FIXME: Fix bugs relating to opening of creative tab, and Optimize this so that the creative tab config doesnt have to be loaded more than once


        if (NeutronTools.CONFIG_CREATIVE_TABS.buildSetup()) {
            NeutronTools.CONFIG_CREATIVE_TABS.load();
            NeutronTools.CONFIG_DISABLED_ITEMS.loadItems();
            NeutronTools.CONFIG_CREATIVE_TABS.setBuildSetup(false);
            if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("simulated")) {
                TabEditConfig tabEditConfig = NeutronTools.CONFIG_CREATIVE_TABS.tabEdits.get(event.getTab());
                tabEditConfig.modifySimulatedContents(event);
            }
        }
        TabEditConfig tabEditConfig = NeutronTools.CONFIG_CREATIVE_TABS.tabEdits.get(event.getTab());
        if (tabEditConfig != null) {
            tabEditConfig.modifyContentsFromEvent(event);
        }
    }


}
