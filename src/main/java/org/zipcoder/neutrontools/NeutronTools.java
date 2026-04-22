package org.zipcoder.neutrontools;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.zipcoder.neutrontools.config.PreInitConfig;
import org.zipcoder.neutrontools.network.SyncConfigPacket;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NeutronTools.MODID)
public class NeutronTools {
    public static final String MODID = "neutrontools";
    public static final String RESOURCE_ID = "neutron";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final PreInitConfig CONFIG = new PreInitConfig();

    public NeutronTools(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Neutrontools) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

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

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent

}
