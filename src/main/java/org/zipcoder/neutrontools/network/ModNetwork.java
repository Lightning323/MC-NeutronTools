package org.zipcoder.neutrontools.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static org.zipcoder.neutrontools.NeutronTools.MODID;

public class ModNetwork {
    private static final String VERSION = "1";
    private static int packetId = 0;

    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "main"),
                () -> VERSION, VERSION::equals, VERSION::equals
        );

        CHANNEL.registerMessage(
                id(),
                SyncConfigPacket.class,
                SyncConfigPacket::encode,
                SyncConfigPacket::decode,
                SyncConfigPacket::handle
        );
    }

    private static int id() {
        return packetId++;
    }

}
