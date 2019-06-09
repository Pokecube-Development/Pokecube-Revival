package pokecube.adventures.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.utils.Tools;

public class ItemTrainer extends Item
{
    public ItemTrainer()
    {
        super();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        if (world.isRemote) { return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand)); }
        Entity target = Tools.getPointedEntity(player, 8);
        if (player.capabilities.isCreativeMode)
        {
            PacketTrainer.sendEditOpenPacket(target, (ServerPlayerEntity) player);
            return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
        }
        return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
    }

}
