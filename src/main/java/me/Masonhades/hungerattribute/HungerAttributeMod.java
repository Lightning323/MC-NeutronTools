package me.Masonhades.hungerattribute;

import me.Masonhades.hungerattribute.attribute.ModAttributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.zipcoder.neutrontools.NeutronTools;


public class HungerAttributeMod {
    public static final String MODID = NeutronTools.MODID;

    public HungerAttributeMod() {
        IEventBus modEnentBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModAttributes.ATTRIBUTES.register(modEnentBus);
    }

}
