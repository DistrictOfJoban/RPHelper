package com.lx862.rphelper.packet;

import com.lx862.rphelper.RPHelper;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ResourcePackSyncPacket(boolean fullRedownload) implements CustomPayload {
    public static final Identifier PACKET_ID = RPHelper.id("sync_rp");
    public static final CustomPayload.Id<ResourcePackSyncPacket> ID = new CustomPayload.Id<>(PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, ResourcePackSyncPacket> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, ResourcePackSyncPacket::fullRedownload, ResourcePackSyncPacket::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
