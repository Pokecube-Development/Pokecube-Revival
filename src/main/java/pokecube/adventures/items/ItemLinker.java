package pokecube.adventures.items;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.events.handlers.EventsHandler;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.lib.CompatWrapper;

public class ItemLinker extends Item
{
    public ItemLinker()
    {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void interact(EntityInteract event)
    {
        ItemStack stack = event.getItemStack();
        if (!CompatWrapper.isValid(stack) || stack.getItem() != this || event.getSide() == Dist.CLIENT) return;
        PlayerEntity playerIn = event.getPlayerEntity();
        Entity target = event.getTarget();
        IGuardAICapability cap = target.getCapability(EventsHandler.GUARDAI_CAP, null);
        if (stack.getItemDamage() == 1 && cap != null)
        {
            boolean canSet = event.getPlayerEntity().isCreative();
            if (target instanceof IEntityOwnable)
            {
                canSet = ((IEntityOwnable) target).getOwner() == playerIn;
            }
            if (stack.hasTag() && canSet)
            {
                Vector4 pos = new Vector4(stack.getTag().getCompound("link"));
                cap.getPrimaryTask().setPos(new BlockPos((int) (pos.x - 0.5), (int) (pos.y - 1), (int) (pos.z - 0.5)));
                playerIn.sendMessage(new StringTextComponent("Set Home to " + pos));
                event.setCanceled(true);
            }
        }
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @Override
    public ActionResultType onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, Hand hand,
            Direction side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        Vector3 hit = Vector3.getNewVector().set(pos);
        Block block = hit.getBlock(worldIn);
        if (block instanceof BlockWarpPad && playerIn.isSneaking() && stack.hasTag())
        {
            TileEntityWarpPad pad = (TileEntityWarpPad) hit.getTileEntity(worldIn);
            if (pad.canEdit(playerIn) && !worldIn.isRemote)
            {
                pad.link = new Vector4(stack.getTag().getCompound("link"));
                playerIn.sendMessage(new StringTextComponent("linked pad to " + pad.link));
            }
            return ActionResultType.SUCCESS;
        }
        else
        {
            if (!worldIn.isRemote)
            {
                if (!stack.hasTag()) stack.setTag(new CompoundNBT());
                CompoundNBT linkTag = new CompoundNBT();
                Vector4 link = new Vector4(hit.x + 0.5, hit.y + 1, hit.z + 0.5, playerIn.dimension);
                link.writeToNBT(linkTag);
                stack.getTag().setTag("link", linkTag);
                playerIn.sendMessage(new StringTextComponent("Saved location " + link));
            }
            else
            {
                StringSelection selection = new StringSelection(hit.intX() + " " + hit.intY() + " " + hit.intZ());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                playerIn.sendMessage(new StringTextComponent("Copied to clipboard"));
            }
        }
        return ActionResultType.FAIL;
    }
}
