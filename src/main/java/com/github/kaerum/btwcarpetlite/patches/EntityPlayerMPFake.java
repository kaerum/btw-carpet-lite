package com.github.kaerum.btwcarpetlite.patches;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

@SuppressWarnings("EntityConstructor")
public class EntityPlayerMPFake extends EntityPlayerMP
{
    public Runnable fixStartingPosition = () -> {};

    public static EntityPlayerMPFake createFake(
            String username,
            MinecraftServer server,
            double x,
            double y,
            double z,
            double yaw,
            double pitch,
            int dimensionId,
            EnumGameType gamemode
        ) throws Exception {
        WorldServer worldIn = server.worldServerForDimension(dimensionId);
        ItemInWorldManager itemInWorldManager = new ItemInWorldManager(worldIn);
        EntityPlayerMPFake instance = new EntityPlayerMPFake(server, worldIn, username, itemInWorldManager);
        instance.fixStartingPosition = () -> instance.setLocationAndAngles(x, y, z, (float)yaw, (float)pitch);
        // TODO:
        server.getConfigurationManager().initializeConnectionToPlayer(new ServerMemoryConnection(), instance);
        instance.setWorld(worldIn);
        instance.setLocationAndAngles(x, y, z, (float)yaw, (float)pitch);
        instance.setEntityHealth(20);
        instance.stepHeight = 0.6F;
        instance.setGameType(gamemode);
//        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance, (byte) (instance.headYaw * 256 / 360)), dimensionId);//instance.dimension);
//        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(instance), dimensionId);//instance.dimension);
//        instance.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f); // show all model layers (incl. capes)
        return instance;
    }

    private EntityPlayerMPFake(MinecraftServer server, World worldIn, String username, ItemInWorldManager itemInWorldManager)
    {
        super(server, worldIn, username, itemInWorldManager);
    }



//    @Override
//    protected void onEquipStack(ItemStack stack)
//    {
//        if (!isUsingItem()) super.onEquipStack(stack);
//    }

    @Override
    public void kill()
    {
        // disconnect fake player
    }


    @Override
    public void onUpdateEntity()
    {
        if (this.mcServer.getTickCounter() % 10 == 0)
        {
//            this.playerNetServerHandler.s();
//            this.worldObj.getChunkManager().updatePosition(this);
//              onTeleportationDone();
        }
        try {
            super.onUpdateEntity();
        }
        catch (NullPointerException ignored)
        {
            // happens with that paper port thingy - not sure what that would fix, but hey
            // the game not gonna crash violently.
        }
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        this.heal(20);
        this.foodStats = new FoodStats();
        kill();
    }

    @Override
    public String getPlayerIP()
    {
        return "127.0.0.1";
    }
}
