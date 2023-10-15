package com.github.kaerum.btwcarpetlite.patches;

import net.minecraft.src.INetworkManager;
import net.minecraft.src.NetHandler;
import net.minecraft.src.Packet;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory NetworkHandler for fake players
 */
public class ServerMemoryConnection implements INetworkManager {

    private static final SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 0);
    private NetHandler netHandler;
    private final List<Packet> queuedPackets = Collections.synchronizedList(new ArrayList());
    private boolean shouldShutdown = false;
    private int maxPacketsPerTick = 2500;

    @Override
    public void setNetHandler(NetHandler handler) {
        this.netHandler = handler;
    }

    @Override
    public void addToSendQueue(Packet packet) {
        if (!this.shouldShutdown) {
            this.processOrCachePacket(packet);
        }
    }

    @Override
    public void wakeThreads() {
        /* empty */
    }

    @Override
    public void processReadPackets() {
        for (int processedPackets = 0; !this.queuedPackets.isEmpty() && (processedPackets < this.maxPacketsPerTick); processedPackets++) {
            this.queuedPackets.remove(0).processPacket(this.netHandler);
        }
    }

    @Override
    public SocketAddress getSocketAddress() {
        return ServerMemoryConnection.socketAddress;
    }

    @Override
    public void serverShutdown() {
        this.shouldShutdown = true;
    }

    @Override
    public int packetSize() {
        return 0;
    }

    @Override
    public void networkShutdown(String var1, Object... var2) {
        this.shouldShutdown = true;
    }

    @Override
    public void closeConnections() {
        this.netHandler = null;
    }

    public void processOrCachePacket(Packet par1Packet)
    {
        if (par1Packet.canProcessAsync() && this.netHandler.canProcessPacketsAsync())
        {
            par1Packet.processPacket(this.netHandler);
        }
        else
        {
            this.queuedPackets.add(par1Packet);
        }
    }
}
