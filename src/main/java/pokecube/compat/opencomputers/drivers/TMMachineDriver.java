package pokecube.compat.opencomputers.drivers;

import java.util.ArrayList;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.blocks.tradingTable.TileEntityTMMachine;

public class TMMachineDriver extends DriverBase
{
    @Override
    public String getComponentName()
    {
        return "tm_machine";
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side)
    {
        TileEntityTMMachine pc = (TileEntityTMMachine) world.getTileEntity(pos);
        return new Environment(pc, getComponentName());
    }

    @Override
    public Class<?> getTileEntityClass()
    {
        return TileEntityTMMachine.class;
    }
    
    public static final class Environment extends ManagedTileEntityEnvironment<TileEntityTMMachine>
    {

        public Environment(TileEntityTMMachine tileEntity, String name)
        {
            super(tileEntity, name);
        }

        @Callback
        @Optional.Method(modid = "opencomputers")
        public Object[] getMovesList(Context context, Arguments args) throws Exception
        {
            if (tileEntity.hasPC() && tileEntity.getPC().isBound())
            {
                InventoryPC inv = tileEntity.getPC().getPC();
                ArrayList<String> moves = tileEntity.getMoves(inv);
                return moves.toArray();
            }
            if (!tileEntity.hasPC()) throw new Exception("no connected PC");
            throw new Exception("connected PC is not bound to a player");
        }

        @Callback
        @Optional.Method(modid = "opencomputers")
        public Object[] applyMove(Context context, Arguments args) throws Exception
        {
            if (tileEntity.hasPC() && tileEntity.getPC().isBound())
            {
                InventoryPC inv = tileEntity.getPC().getPC();
                ArrayList<String> moves = tileEntity.getMoves(inv);
                String move = args.checkString(0);
                for (String s : moves)
                {
                    if (s.equalsIgnoreCase(move))
                    {
                        tileEntity.addMoveToTM(s);
                        return new Object[] {};
                    }
                }
                throw new Exception("requested move not found");
            }
            if (!tileEntity.hasPC()) throw new Exception("no connected PC");
            throw new Exception("connected PC is not bound to a player");
        }
    }
}
