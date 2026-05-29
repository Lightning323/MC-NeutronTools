package org.lightning.neutrontools;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lightning.neutrontools.config.NeutronDisabledItemConfig;
import org.lightning.neutrontools.config.creativeTabs.NeutronCreativeTabConfig;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;
import org.lightning.neutrontools.events.ClientModEvents;
import org.lightning.neutrontools.packs.GlobalPackHandler;
import org.slf4j.Logger;
import org.lightning.neutrontools.config.NeutronConfig;
import org.lightning.neutrontools.network.SyncConfigPacket;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NeutronTools.MODID)
public class NeutronTools {
    public static final String MODID = "neutrontools";
    public static final Logger LOG = LogUtils.getLogger();

    public static File BASE_GAME_DIRECTORY = FMLPaths.GAMEDIR.get().toFile();
    public static Path BASE_CONFIG_DIRECTORY = Paths.get(FMLPaths.CONFIGDIR.get().toString(), MODID);

    public static Path OPTIONAL_PACK_DIRECTORY = Paths.get(BASE_CONFIG_DIRECTORY.toString(), "packs_optional");
    public static Path PACK_DIRECTORY = Paths.get(BASE_CONFIG_DIRECTORY.toString(), "packs_required");

    public static final NeutronConfig CONFIG = new NeutronConfig();
    public static final NeutronCreativeTabConfig CONFIG_CREATIVE_TABS = new NeutronCreativeTabConfig();
    public static final NeutronDisabledItemConfig CONFIG_DISABLED_ITEMS = new NeutronDisabledItemConfig();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public NeutronTools(IEventBus eventBus, ModContainer container) {
        // Register the commonSetup method for modloading
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(NeutronCreativeTabs::registerTabs);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Neutrontools) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        eventBus.addListener(EventPriority.LOWEST, ClientModEvents::buildContents);
        CREATIVE_MODE_TABS.register(eventBus);
        eventBus.addListener(NeutronTools::addPackSources);
    }

    private static void addPackSources(AddPackFindersEvent event) {
        Path optionalResources = Paths.get(OPTIONAL_PACK_DIRECTORY.toString(), "resource_packs");
        Path optionalData =  Paths.get(OPTIONAL_PACK_DIRECTORY.toString(), "data_packs");
        Path requiredResources = Paths.get(PACK_DIRECTORY.toString(), "resource_packs");
        Path requiredData =  Paths.get(PACK_DIRECTORY.toString(), "data_packs");

        if(!optionalResources.toFile().exists()) optionalResources.toFile().mkdirs();
        if(!optionalData.toFile().exists()) optionalData.toFile().mkdirs();
        if(!requiredResources.toFile().exists()) requiredResources.toFile().mkdirs();
        if(!requiredData.toFile().exists()) requiredData.toFile().mkdirs();

        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(GlobalPackHandler.createForcedRepositorySource(PackType.SERVER_DATA, requiredData));
            event.addRepositorySource(GlobalPackHandler.createRepositorySource(PackType.SERVER_DATA, optionalData));
        }
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource(GlobalPackHandler.createForcedRepositorySource(PackType.CLIENT_RESOURCES, requiredResources));
            event.addRepositorySource(GlobalPackHandler.createRepositorySource(PackType.CLIENT_RESOURCES, optionalResources));
        }
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
            LOG.info("Syncing config with client...");
            PacketDistributor.sendToPlayer(player, new SyncConfigPacket(CONFIG));
        }
    }
}
