package pokecube.compat.opencomputers.drivers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;

public class SiphonDriver extends DriverBase
{
    @Override
    public String getComponentName()
    {
        return "pokesiphon";
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side)
    {
        TileEntitySiphon pc = (TileEntitySiphon) world.getTileEntity(pos);
        return new Environment(pc, getComponentName());
    }

    @Override
    public Class<?> getTileEntityClass()
    {
        return TileEntitySiphon.class;
    }
    
    public static final class Environment extends ManagedTileEntityEnvironment<TileEntitySiphon>
    {

        public Environment(TileEntitySiphon tileEntity, String name)
        {
            super(tileEntity, name);
        }

        @Callback
        public Object[] getPower(Context context, Arguments args)
        {
            return new Object[] { tileEntity.currentOutput };
        }
    }
}
