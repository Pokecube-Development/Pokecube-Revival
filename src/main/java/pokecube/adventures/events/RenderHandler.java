package pokecube.adventures.events;

import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.container.ContainerBase;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.commands.Config;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.lib.CompatWrapper;

@OnlyIn(Dist.CLIENT)
public class RenderHandler
{
    public static float partialTicks = 0.0F;

    public RenderHandler()
    {
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent evt)
    {
        PlayerEntity player = evt.getPlayerEntity();
        ItemStack stack = evt.getItemStack();
        if (!CompatWrapper.isValid(stack)) return;
        CompoundNBT tag = stack.hasTag() ? stack.getTag() : new CompoundNBT();
        if (tag.getBoolean("isapokebag"))
        {
            evt.getToolTip().add("PokeBag");
        }
        if (tag.hasKey("dyeColour"))
        {
            String colour = I18n.format(EnumDyeColor.byDyeDamage(tag.getInt("dyeColour")).getUnlocalizedName());
            boolean has = false;
            for (String s : evt.getToolTip())
            {
                has = s.equals(colour);
                if (has) break;
            }
            if (!has) evt.getToolTip().add(colour);
        }
        if (player == null || player.openContainer == null) return;
        if (player.openContainer instanceof ContainerBase
                || (GuiScreen.isShiftKeyDown() && !ClonerHelper.getGeneSelectors(stack).isEmpty()))
        {
            IMobGenetics genes = ClonerHelper.getGenes(stack);
            int index = ClonerHelper.getIndex(stack);
            if (genes != null)
            {
                for (Alleles a : genes.getAlleles().values())
                {
                    evt.getToolTip().add(a.getExpressed().getKey().getResourcePath() + ": " + a.getExpressed());
                    if (Config.instance.expandedDNATooltips)
                    {
                        evt.getToolTip().add("    " + a.getAlleles()[0]);
                        evt.getToolTip().add("    " + a.getAlleles()[1]);
                    }
                }
            }
            if (index != -1)
            {
                evt.getToolTip().add("I: " + index);
            }
            Set<Class<? extends Gene>> genesSet;
            if (!(genesSet = ClonerHelper.getGeneSelectors(stack)).isEmpty())
            {
                for (Class<? extends Gene> geneC : genesSet)
                {
                    try
                    {
                        Gene gene = geneC.newInstance();
                        evt.getToolTip().add(gene.getKey().getResourcePath());
                    }
                    catch (InstantiationException | IllegalAccessException e)
                    {

                    }
                }
            }
            if (tag.hasKey("ivs"))
            {
                evt.getToolTip()
                        .add("" + tag.getLong("ivs") + ":" + tag.getFloat("size") + ":" + tag.getByte("nature"));
            }
            if (RecipeSelector.isSelector(stack))
            {
                SelectorValue value = ClonerHelper.getSelectorValue(stack);
                value.addToTooltip(evt.getToolTip());
            }
        }
    }
}
