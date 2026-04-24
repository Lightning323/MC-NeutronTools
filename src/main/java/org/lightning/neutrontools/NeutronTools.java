package org.lightning.neutrontools;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;
import org.slf4j.Logger;
import org.lightning.neutrontools.config.NeutronConfig;
import org.lightning.neutrontools.network.SyncConfigPacket;

import java.io.File;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NeutronTools.MODID)
public class NeutronTools {
    public static final String MODID = "neutrontools";
    public static final File CONFIG_PATH = new File(FMLPaths.CONFIGDIR.get().toFile(), "/" + MODID);

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final NeutronConfig CONFIG = new NeutronConfig();
    public static final NeutronCreativeTabs TABS = new NeutronCreativeTabs();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);


    private void registerTabs(RegisterEvent event) {
        // Check if we are currently in the Creative Mode Tab registry phase
        if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
            TABS.newTabs.forEach((tabKey, tab) -> {
                String originalTabKey = tabKey == null ? "unknown" : tabKey;
                try {
                    if (tabKey.contains(":")) {
                        tabKey = tabKey.split(":")[1];
                    }
                    //We need to replace all invalid characters
                    tabKey = tabKey.replaceAll("[^a-z0-9/._-]", "");

                    if (tabKey == null) {
                        LOGGER.error("Tab name key is null");
                        return;
                    }
                    LOGGER.info("Registering new tab {}", tabKey);
                    event.register(Registries.CREATIVE_MODE_TAB, resource(tabKey), () -> tab);
                } catch (Exception e) {
                    LOGGER.error("Failed to register new tab \"{}\"", originalTabKey, e);
                }
            });
        }
    }


    public NeutronTools(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerTabs);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Neutrontools) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LOGGER.info("Syncing config with client...");
            PacketDistributor.sendToPlayer(player, new SyncConfigPacket(CONFIG));
        }
    }
}
