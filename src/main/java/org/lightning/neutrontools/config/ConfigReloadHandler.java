package org.lightning.neutrontools.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.lightning.neutrontools.NeutronTools;

import static org.lightning.neutrontools.NeutronTools.CONFIG_DISABLED_ITEMS;
import static org.lightning.neutrontools.NeutronTools.CONFIG_UNBREAKABLE_ITEMS;

// Must be on the GAME bus (NeoForge.EVENT_BUS)
@EventBusSubscriber(modid = NeutronTools.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ConfigReloadHandler {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {

        // Add a simple synchronous reload listener directly to the event
        event.addListener(new ResourceManagerReloadListener() {
            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                // Runs on the main thread whenever /reload is executed
                CONFIG_UNBREAKABLE_ITEMS.reloadFromDisk();
                CONFIG_DISABLED_ITEMS.reloadFromDisk();
            }
        });
    }

}