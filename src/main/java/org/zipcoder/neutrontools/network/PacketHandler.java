package org.zipcoder.neutrontools.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import static org.zipcoder.neutrontools.NeutronTools.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // "1" is your protocol version
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SyncConfigPacket.TYPE,
                SyncConfigPacket.CODEC,
                SyncConfigPacket::handle
        );

    }
}