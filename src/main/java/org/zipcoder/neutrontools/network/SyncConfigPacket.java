package org.zipcoder.neutrontools.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.config.PreInitConfig;

public record SyncConfigPacket(float hungerMultiplier) implements CustomPacketPayload {

    public static final Type<SyncConfigPacket> TYPE =
            new Type<>(NeutronTools.resource("sync_config"));

    public static final StreamCodec<FriendlyByteBuf, SyncConfigPacket> CODEC =
            StreamCodec.of(
                    //Writing
                    (buf, packet) -> buf.writeFloat(packet.hungerMultiplier),
                    //Reading
                    buf -> new SyncConfigPacket(buf.readFloat())
            );

    public SyncConfigPacket(PreInitConfig config) {
        this(config.hungerMultiplier);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncConfigPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Client-side update
            NeutronTools.CONFIG.hungerMultiplier = packet.hungerMultiplier();
        });
    }
}