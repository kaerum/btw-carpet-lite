package com.github.kaerum.btwcarpetlite.mixin;

import com.github.kaerum.btwcarpetlite.utils.TickSpeed;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.logging.Logger;

@Mixin(MinecraftServer.class)
public class MinecraftServer_tickspeed {
    @Shadow private volatile boolean running;

    @Shadow private long timeReference;

    //@Shadow private boolean profilerStartQueued;

    @Shadow @Final private Profiler profiler;

    public MinecraftServer_tickspeed()
    {
        super();
    }

    @Shadow protected abstract void tick(BooleanSupplier booleanSupplier_1);

    @Shadow protected abstract boolean shouldKeepTicking();

    //@Shadow private long field_19248;

    //@Shadow protected abstract void method_16208();

    @Shadow private volatile boolean loading;

    //@Shadow protected abstract void startMonitor(TickDurationMonitor monitor);

    @Shadow private long lastTimeReference;
    @Shadow private boolean waitingForNextTick;

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow private boolean needsDebugSetup;
    @Shadow private boolean needsRecorderSetup;
    @Shadow private int ticks;

    @Shadow protected abstract void startTickMetrics();

    @Shadow protected abstract void runTasksTillTickEnd();

    @Shadow private long nextTickTimestamp;
    @Shadow @Final
    private static Logger LOGGER;
    private float carpetMsptAccum = 0.0f;

    /**
     * To ensure compatibility with other mods we should allow milliseconds
     */

    // Cancel a while statement
    @Redirect(method = "runServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;running:Z"))
    private boolean cancelRunLoop(MinecraftServer server)
    {
        return false;
    }

    // Replaced the above cancelled while statement with this one
    // could possibly just inject that mspt selection at the beginning of the loop, but then adding all mspt's to
    // replace 50L will be a hassle
    @Inject(method = "runServer", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V"))
    private void modifiedRunLoop(CallbackInfo ci)
    {
        while (this.running)
        {
            //CM deciding on tick speed
            long msThisTick = 0L;
            long long_1 = 0L;
            if (TickSpeed.tickWarpStartTime != 0 && TickSpeed.continueWarp((MinecraftServer) (Object)this))
            {
                //making sure server won't flop after the warp or if the warp is interrupted
                this.timeReference = this.lastTimeReference = Util.getMeasuringTimeMs();
                carpetMsptAccum = TickSpeed.mspt;
            }
            else
            {
                if (Math.abs(carpetMsptAccum - TickSpeed.mspt) > 1.0f)
                {
                	// Tickrate changed. Ensure that we use the correct value.
                	carpetMsptAccum = TickSpeed.mspt;
                }

                msThisTick = (long)carpetMsptAccum; // regular tick
                carpetMsptAccum += TickSpeed.mspt - msThisTick;

                long_1 = Util.getMeasuringTimeMs() - this.timeReference;
            }
            //end tick deciding
            //smoothed out delay to include mcpt component. With 50L gives defaults.
            if (long_1 > 1000L + 20 * TickSpeed.mspt && this.timeReference - this.lastTimeReference >= /*15000L*/10000L+100*TickSpeed.mspt)
            {
                long long_2 = (long)(long_1 / TickSpeed.mspt); //50L;
                LOGGER.warning("Can't keep up! Is the server overloaded? Running " + long_1 + "ms or " + long_2 + " ticks behind");
                this.timeReference += (long)(long_2 * TickSpeed.mspt);//50L;
                this.lastTimeReference = this.timeReference;
            }

            if (needsDebugSetup) {
                this.needsDebugSetup = false;
            }

            this.timeReference += msThisTick;//50L;
            startTickMetrics();
            this.profiler.push("tick");
            this.tick(TickSpeed.time_warp_start_time != 0 ? ()->true : this::shouldKeepTicking);
            this.profiler.swap("nextTickWait");
            if (TickSpeed.time_warp_start_time != 0) // clearing all hanging tasks no matter what when warping
            {
                while(this.runEveryTask()) {Thread.yield();}
            }
            this.waitingForNextTick = true;
            this.nextTickTimestamp = Math.max(Util.getMeasuringTimeMs() + /*50L*/ msThisTick, this.timeReference);
            this.runTasksTillTickEnd();
            this.profiler.pop();
            this.profiler.endTick();
            this.loading = true;
        }
    }
    private boolean runEveryTask() {
        if (super.runTask()) {
            return true;
        } else {
            if (true) { // unconditionally this time
                for(WorldServer serverlevel : getWorlds()) {
                    if (serverlevel.()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
