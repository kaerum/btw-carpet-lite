package com.github.kaerum.btwcarpetlite.utils;

import com.github.kaerum.btwcommander.adaptations.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICommandManager;
import net.minecraft.src.ICommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TickSpeed {
    public static float tickrate = 20.0f;
    public static float mspt = 50.0f;
    public static long timeBias = 0;
    public static long timeWarpStartTime = 0;
    public static long timeWarpScheduledTicks = 0;
    public static EntityPlayer timeAdvancerer = null;
    public static String tickWarpCallback = null;
    public static ServerCommandSource tickWarpSender = null;

    /**
     * Functional interface that listens for tickrate changes. This is
     * implemented to allow tickrate compatibility with other mods etc.
     */
    private static final Map<String, BiConsumer<String, Float>> tickrateListeners = new HashMap<>();
    private static final float MIN_TICKRATE = 0.01f;

    public static String tickrateAdvance(MinecraftServer server, EntityPlayer player, int advance, String callback, ServerCommandSource source)
    {
        if (0 == advance)
        {
            tickWarpCallback = null;
            tickWarpSender = null;
            finishTimeWarp(server);
            return Messenger.c("gi Warp interrupted");
        }
        if (timeBias > 0)
        {
            String who = "Another player";
            if (timeAdvancerer != null) who = timeAdvancerer.getEntityName();
            return Messenger.c("l "+who+" is already advancing time at the moment. Try later or ask them");
        }
        timeAdvancerer = player;
        timeWarpStartTime = System.nanoTime();
        timeWarpScheduledTicks = advance;
        timeBias = advance;
        tickWarpCallback = callback;
        tickWarpSender = source;
        return Messenger.c("gi Warp speed ....");
    }

    public static void finishTimeWarp(MinecraftServer server)
    {

        long completed_ticks = timeWarpScheduledTicks - timeBias;
        double milis_to_complete = System.nanoTime()- timeWarpStartTime;
        if (milis_to_complete == 0.0)
        {
            milis_to_complete = 1.0;
        }
        milis_to_complete /= 1000000.0;
        int tps = (int) (1000.0D*completed_ticks/milis_to_complete);
        double mspt = (1.0*milis_to_complete)/completed_ticks;
        timeWarpScheduledTicks = 0;
        timeWarpStartTime = 0;
        if (tickWarpCallback != null)
        {
            ICommandManager icommandmanager = tickWarpSender.getServer().getCommandManager();
            try
            {
                icommandmanager.executeCommand((ICommandSender) tickWarpSender, tickWarpCallback);
            }
            catch (Throwable var23)
            {
                if (timeAdvancerer != null)
                {
                    Messenger.m(timeAdvancerer, "r Command Callback failed - unknown error: ", "rb /"+ tickWarpCallback,"/"+ tickWarpCallback);
                }
            }
            tickWarpCallback = null;
            tickWarpSender = null;
        }
        if (timeAdvancerer != null)
        {
            Messenger.m(timeAdvancerer, String.format("gi ... Time warp completed with %d tps, or %.2f mspt",tps, mspt ));
            timeAdvancerer = null;
        }
        else
        {
            Messenger.print_server_message(server, String.format("... Time warp completed with %d tps, or %.2f mspt",tps, mspt ));
        }
        timeBias = 0;

    }

    public static boolean continueWarp(MinecraftServer server)
    {
        if (timeBias > 0)
        {
            if (timeBias == timeWarpScheduledTicks) //first call after previous tick, adjust start time
            {
                timeWarpStartTime = System.nanoTime();
            }
            timeBias -= 1;
            return true;
        }
        else
        {
            finishTimeWarp(server);
            return false;
        }
    }

    public static void tickrate(float rate)
    {
        tickrate = rate;
        long mspt = (long)(1000.0 / tickrate);
        if (mspt <= 0L)
        {
            mspt = 1L;
            tickrate = 1000.0f;
        }

        TickSpeed.mspt = (float)mspt;
        notifyTickrateListeners("btwcarpetlite");
    }

    private static void tickrateChanged(String modId, float rate)
    {
    	// Other mods might change the tickrate in a slightly
    	// different way. Also allow for tickrates that don't
    	// divide into 1000 here.

        if (rate < MIN_TICKRATE)
        {
            rate = MIN_TICKRATE;
        }

        tickrate = rate;
        mspt = 1000.0f / tickrate;
        notifyTickrateListeners(modId);
    }

    private static void notifyTickrateListeners(String originModId)
    {
        synchronized (tickrateListeners)
        {
            for (Map.Entry<String, BiConsumer<String, Float>> listenerEntry : tickrateListeners.entrySet())
            {
                if (originModId == null || !originModId.equals(listenerEntry.getKey()))
                {
                    listenerEntry.getValue().accept(originModId, tickrate);
                }
            }
        }
    }

    public static BiConsumer<String, Float> addTickrateListener(String modId, BiConsumer<String, Float> tickrateListener)
    {
        synchronized (tickrateListeners)
        {
            tickrateListeners.put(modId, tickrateListener);
        }
        return TickSpeed::tickrateChanged;
    }
}
