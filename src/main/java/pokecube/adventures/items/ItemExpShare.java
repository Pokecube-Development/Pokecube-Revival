package pokecube.adventures.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemExpShare extends Item
{
    public ItemExpShare()
    {
        super();
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> list, ITooltipFlag advanced)
    {
        if (GuiScreen.isShiftKeyDown()) list.add(I18n.format("pokecube.expshare.tooltip"));
        else list.add(I18n.format("pokecube.tooltip.advanced"));
    }

}
