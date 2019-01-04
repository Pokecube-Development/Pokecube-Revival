package pokecube.compat.opencomputers.drivers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import thut.api.network.PacketHandler;

public class AFADriver extends DriverBase
{
    @Override
    public String getComponentName()
    {
        return "afa";
    }

    public boolean canConnectNode(EnumFacing side)
    {
        return side == EnumFacing.DOWN;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side)
    {
        if (!canConnectNode(side)) return null;
        TileEntityAFA pc = (TileEntityAFA) world.getTileEntity(pos);
        return new Environment(pc, getComponentName());
    }

    @Override
    public Class<?> getTileEntityClass()
    {
        return TileEntityAFA.class;
    }

    public static final class Environment extends ManagedTileEntityEnvironment<TileEntityAFA>
    {

        public Environment(TileEntityAFA tileEntity, String name)
        {
            super(tileEntity, name);
        }


        @Callback(doc = "function(scale:number, dx:number, dy:number, dz:number)- Sets the parameters for the hologram.")
        @Optional.Method(modid = "opencomputers")
        public Object[] setHoloState(Context context, Arguments args)
        {
            tileEntity.scale = args.checkInteger(0);
            tileEntity.shift[0] = args.checkInteger(1);
            tileEntity. shift[1] = args.checkInteger(2);
            tileEntity.shift[2] = args.checkInteger(3);
            if (!tileEntity.getWorld().isRemote)
            {
                PacketHandler.sendTileUpdate(tileEntity);
            }
            return new Object[0];
        }

        @Callback(doc = "function(range:number) - sets the radius of affect")
        @Optional.Method(modid = "opencomputers")
        public Object[] setRange(Context context, Arguments args)
        {
            tileEntity.distance = args.checkInteger(0);
            if (!tileEntity.getWorld().isRemote)
            {
                PacketHandler.sendTileUpdate(tileEntity);
            }
            return new Object[] { tileEntity.distance };
        }

        @Callback(doc = "Returns the current loaded ability")
        @Optional.Method(modid = "opencomputers")
        public Object[] getAbility(Context context, Arguments args) throws Exception
        {
            if (tileEntity.ability != null)
            {
                String arg = tileEntity.ability.toString();
                return new Object[] { arg };
            }
            throw new Exception("no ability");
        }

        @Callback(doc = "Returns the amount of stored energy")
        @Optional.Method(modid = "opencomputers")
        public Object[] getEnergy(Context context, Arguments args)
        {
            return new Object[] { tileEntity.energy };
        }

        @Callback(doc = "Returns the current set range")
        @Optional.Method(modid = "opencomputers")
        public Object[] getRange(Context context, Arguments args)
        {
            return new Object[] { tileEntity.distance };
        }
    }
}
