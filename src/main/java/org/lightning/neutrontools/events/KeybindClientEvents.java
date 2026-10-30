package org.lightning.neutrontools.events;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.utils.KeybindUtils;

@EventBusSubscriber(
        modid = NeutronTools.MODID,
        bus = EventBusSubscriber.Bus.GAME,
        value = Dist.CLIENT
)
public class KeybindClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        //Enforce disabled keybinds staying unbound even if rebound at runtime
        KeybindUtils.unbindHiddenKeyBinds();
    }
}