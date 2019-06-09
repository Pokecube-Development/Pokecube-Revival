package pokecube.adventures.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import thut.api.maths.Vector3;

public class ItemTarget extends Item
{
    public ItemTarget()
    {
        super();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        if (world.isRemote) { return new ActionResult<>(ActionResultType.PASS, itemstack); }

        Vector3 location = Vector3.getNewVector().set(player).add(Vector3.getNewVector().set(player.getLookVec()))
                .add(0, 1.62, 0);
        EntityTarget t = new EntityTarget(world);
        location.moveEntity(t);
        world.spawnEntity(t);

        return new ActionResult<>(ActionResultType.PASS, itemstack);
    }
}
