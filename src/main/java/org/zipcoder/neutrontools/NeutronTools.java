package org.zipcoder.neutrontools;

import com.mojang.logging.LogUtils;
import org.zipcoder.creativetabs.CreativeTabs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.zipcoder.neutrontools.config.PreInitConfig;
import org.zipcoder.neutrontools.network.ModNetwork;
import org.zipcoder.neutrontools.network.SyncConfigPacket;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(NeutronTools.MODID)
public class NeutronTools {
    public static final String MODID = "neutrontools";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final PreInitConfig CONFIG = new PreInitConfig();

    public static float clamp(float value, float min, float max) {
        if (value > max) return max;
        else if (value < min) return min;
        return value;
    }

    public static double clamp(double value, double min, double max) {
        if (value > max) return max;
        else if (value < min) return min;
        return value;
    }

    public NeutronTools() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ModNetwork.register();
        CreativeTabs mct = new CreativeTabs();
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LOGGER.info("Syncing config with client...");
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncConfigPacket(CONFIG));
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
