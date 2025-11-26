package org.zipcoder.neutrontools;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.logging.LogUtils;
import me.Masonhades.hungerattribute.HungerAttributeMod;
import me.Masonhades.hungerattribute.attribute.ModAttributes;
import me.Masonhades.hungerattribute.event.HungerDataHandler;
import me.hypherionmc.morecreativetabs.MoreCreativeTabs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
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
import org.slf4j.Logger;
import org.zipcoder.neutrontools.config.PreInitConfig;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(NeutronTools.MODID)
public class NeutronTools {
    public static final String MODID = "neutrontools";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final PreInitConfig CONFIG = new PreInitConfig();

    public NeutronTools() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //Register creative tabs
//        TEST_CREATIVE_TABS.register(modEventBus);

        //Setup MCT mod
        MoreCreativeTabs mct = new MoreCreativeTabs();
        HungerAttributeMod hunger = new HungerAttributeMod();
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (CONFIG.setHungerMultiplier) {
            float val = CONFIG.hungerMultiplier;
            setHungerMultiplier(player,val);
        }
    }

    public static void setHungerMultiplier(Player player, float val) {
        if (val > 100) val = 100;
        else if (val < 0f) val = 0f;
        AttributeInstance attribute = player.getAttribute(ModAttributes.HUNGER_MULTIPLIER.get());
        if (attribute == null) {
            LOGGER.error("Failed to set hunger multiplier for {}", player.getDisplayName().getString());
            return;
        }
        attribute.setBaseValue(val);
        HungerDataHandler.save(player, val);
        LOGGER.debug("Set hunger multiplier for {} to {}", player.getDisplayName().getString(), val);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
