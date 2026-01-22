//package org.zipcoder.neutrontools.events;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.world.item.CreativeModeTab;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import org.zipcoder.neutrontools.NeutronTools;
//import org.zipcoder.neutrontools.creativetabs.CreativeTabs;
//import org.zipcoder.neutrontools.utils.CreativeTabUtils;
//
//import java.util.Collection;
//import java.util.HashMap;
//
//@Mod.EventBusSubscriber(modid = NeutronTools.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
//public class CreativeTabReadyDetector {
//
//    private static int totalTabs = -1;
//    private static int eventsSeen = 0;
//    private static boolean done = false;
//
//
//    public static final HashMap<CreativeModeTab, Collection<ItemStack>> cachedCreativeTabItems = new HashMap<>();
//
//    @SubscribeEvent
//    public static void onBuildTabs(BuildCreativeModeTabContentsEvent event) {
//        // First time this event fires = creative tabs are ready
//
//        if (!event.getTab().getDisplayItems().isEmpty())
//            cachedCreativeTabItems.put(event.getTab(), event.getTab().getDisplayItems());
//
//        NeutronTools.LOGGER.debug("Build tabs event for tab {}, Items {}", CreativeTabUtils.getTranslationKey(event.getTab()), cachedCreativeTabItems.get(event.getTab()));
//
//        // First event: determine how many tabs exist
//        if (totalTabs == -1) {
//            totalTabs = Minecraft.getInstance()
//                    .level
//                    .registryAccess()
//                    .registryOrThrow(Registries.CREATIVE_MODE_TAB)
//                    .size();
//        }
//        NeutronTools.LOGGER.debug("Tab {} has {} items", CreativeTabUtils.getTranslationKey(event.getTab()), event.getTab().getDisplayItems().size());
//        NeutronTools.LOGGER.debug("Registered items: {}", event.getTab().getRecipeItems());
//        NeutronTools.LOGGER.debug("Display items: {}", event.getTab().getDisplayItems());
//        NeutronTools.LOGGER.debug("All items: {}", event.getTab().getItemList());
//
//        eventsSeen++;
//
//        // When we've seen all tab events, tabs are fully built
//        if (!done && eventsSeen >= totalTabs) {
//            done = true;
//
//            NeutronTools.LOGGER.info("All creative tabs finished building. Scheduling post-build task.");
//
//            // Defer to next tick to avoid recursion
//            Minecraft.getInstance().tell(() -> {
//                CreativeTabs.reloadTabs();
//                NeutronTools.LOGGER.info("Post-build creative tab reload completed.");
//            });
//        }
//    }
//}