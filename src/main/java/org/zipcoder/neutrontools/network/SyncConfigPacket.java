package org.zipcoder.neutrontools.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.config.PreInitConfig;

import java.util.function.Supplier;

public class SyncConfigPacket {
    public  int portalWaitTime;
    public  float hungerMultiplier;

    public SyncConfigPacket(PreInitConfig config) {
        this.portalWaitTime = config.portalWaitTime;
        this.hungerMultiplier = config.hungerMultiplier;
    }

    public SyncConfigPacket() {
    }

    public static void encode(SyncConfigPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.portalWaitTime);
        buf.writeFloat(packet.hungerMultiplier);
    }

    public static SyncConfigPacket decode(FriendlyByteBuf buf) {
        SyncConfigPacket scp =  new SyncConfigPacket();
        scp.portalWaitTime = buf.readInt();
        scp.hungerMultiplier = buf.readFloat();
        return scp;
    }

    public static void handle(SyncConfigPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //This is happening on the client
            NeutronTools.CONFIG.hungerMultiplier = packet.hungerMultiplier;
            NeutronTools.CONFIG.portalWaitTime = packet.portalWaitTime;
        });
        ctx.get().setPacketHandled(true);
    }
}
