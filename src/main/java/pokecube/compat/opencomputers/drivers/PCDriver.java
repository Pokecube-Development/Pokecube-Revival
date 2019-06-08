package pokecube.compat.opencomputers.drivers;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.blocks.pc.TileEntityPC;

public class PCDriver extends DriverBase
{
    @Override
    public String getComponentName()
    {
        return "pokecubepc";
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side)
    {
        TileEntityPC pc = (TileEntityPC) world.getTileEntity(pos);
        return new Environment(pc, getComponentName());
    }

    @Override
    public Class<?> getTileEntityClass()
    {
        return TileEntityPC.class;
    }
    
    public static final class Environment extends ManagedTileEntityEnvironment<TileEntityPC>
    {

        public Environment(TileEntityPC tileEntity, String name)
        {
            super(tileEntity, name);
        }

        @Callback(doc = "Returns the items in the PC")
        public Object[] getItemList(Context context, Arguments args) throws Exception
        {
            if (tileEntity.isBound())
            {
                InventoryPC inv = tileEntity.getPC();
                ArrayList<Object> items = Lists.newArrayList();
                for (int i = 0; i < inv.getSizeInventory(); i++)
                {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (stack != ItemStack.EMPTY) items.add(stack.getDisplayName());
                }
                return items.toArray();
            }
            throw new Exception("PC not bound");
        }
    }
}
