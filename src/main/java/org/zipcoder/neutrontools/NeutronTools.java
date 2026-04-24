package org.zipcoder.neutrontools;

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
import org.slf4j.Logger;
import org.zipcoder.neutrontools.config.NeutronConfig;
import org.zipcoder.neutrontools.network.SyncConfigPacket;

import java.io.File;
import java.util.HashMap;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NeutronTools.MODID)
public class NeutronTools {
    public static final String MODID = "neutrontools";
    public static final String RESOURCE_ID = "neutron";
    public static final File CONFIG_PATH = new File(FMLPaths.CONFIGDIR.get().toFile(), "/neutron");

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final NeutronConfig CONFIG = new NeutronConfig();


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    //This list doesnt get reset
    public static final HashMap<String, CreativeModeTab> NEW_TABS = new HashMap<>();

    private void registerTabs(RegisterEvent event) {
        // Check if we are currently in the Creative Mode Tab registry phase
        if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
//            CreativeModeTab myTab = CreativeModeTab.builder()
//                    .title(Component.translatable("itemGroup." + MODID + ".example_tab"))
//                    .icon(() -> new ItemStack(Items.ACACIA_BOAT))
//                    .displayItems((parameters, output) -> {
//                        output.accept(Items.ACACIA_BOAT);
//                        output.accept(Items.DIAMOND);
//                    })
//                    .build();

            NEW_TABS.forEach((tabKey, tab) -> {
                if (tabKey == null) {
                    LOGGER.error("Tab name key is null");
                    return;
                }
                LOGGER.info("Registering new tab {}", tabKey);
                event.register(Registries.CREATIVE_MODE_TAB, resource(tabKey), () -> tab);
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

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent

}
