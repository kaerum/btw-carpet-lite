package com.github.kaerum.btwcarpetlite.patches;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.Packet;

public class NetServerHandlerFake extends NetServerHandler {
    public NetServerHandlerFake(MinecraftServer par1, INetworkManager par2, EntityPlayerMP par3) {
        super(par1, par2, par3);
    }

    @Override
    public void sendPacket(Packet packet) {}

    @Override
    public void kickPlayerFromServer(String par1Str) {}
}
