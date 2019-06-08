package pokecube.compat.opencomputers.drivers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import thut.api.maths.Vector4;

public class WarpPadDriver extends DriverBase
{
    @Override
    public String getComponentName()
    {
        return "warppad";
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side)
    {
        TileEntityWarpPad pc = (TileEntityWarpPad) world.getTileEntity(pos);
        return new Environment(pc, getComponentName());
    }

    @Override
    public Class<?> getTileEntityClass()
    {
        return TileEntityWarpPad.class;
    }
    
    public static final class Environment extends ManagedTileEntityEnvironment<TileEntityWarpPad>
    {

        public Environment(TileEntityWarpPad tileEntity, String name)
        {
            super(tileEntity, name);
        }

        @Callback(doc = "Returns the current 4-vector destination")
        public Object[] getDestination(Context context, Arguments args) throws Exception
        {
            if (tileEntity.link != null) { return new Object[] { tileEntity.link.x, tileEntity.link.y, tileEntity.link.z, tileEntity.link.w }; }
            throw new Exception("no link");
        }

        @Callback(doc = "function(x:number, y:number, z:number, w:number) - Sets the 4-vector destination, w is the dimension")
        public Object[] setDestination(Context context, Arguments args) throws Exception
        {
            if (args.isDouble(0) && args.isDouble(1) && args.isDouble(2) && args.isDouble(3))
            {
                float x = (float) args.checkDouble(0);
                float y = (float) args.checkDouble(1);
                float z = (float) args.checkDouble(2);
                float w = (float) args.checkDouble(3);
                if (tileEntity.link == null)
                {
                    tileEntity.link = new Vector4(x, y, z, w);
                }
                else
                {
                    tileEntity.link.set(x, y, z, w);
                }
                return new Object[] { tileEntity.link.x, tileEntity.link.y, tileEntity.link.z, tileEntity.link.w };
            }
            throw new Exception("invalid arguments, expected number,number,number,number");
        }

    }
}
