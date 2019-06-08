package pokecube.compat.opencomputers.drivers;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.crafting.PoweredProcess;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.blocks.cloner.recipe.RecipeSplice;
import pokecube.adventures.blocks.cloner.tileentity.TileEntitySplicer;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;

public class SplicerDriver extends DriverBase
{
    @Override
    public String getComponentName()
    {
        return "dnaSplicer";
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side)
    {
        TileEntitySplicer pc = (TileEntitySplicer) world.getTileEntity(pos);
        return new Environment(pc, getComponentName());
    }

    @Override
    public Class<?> getTileEntityClass()
    {
        return TileEntitySplicer.class;
    }

    public static final class Environment extends ManagedTileEntityEnvironment<TileEntitySplicer>
    {

        public Environment(TileEntitySplicer tileEntity, String name)
        {
            super(tileEntity, name);
        }

        @Callback
        public Object[] getSourceInfo(Context context, Arguments args) throws Exception
        {
            IMobGenetics genes = ClonerHelper.getGenes(tileEntity.getStackInSlot(2));
            if (genes == null) throw new Exception("No Genes found in source slot.");
            List<String> values = Lists.newArrayList();
            for (ResourceLocation l : genes.getAlleles().keySet())
            {
                Alleles a = genes.getAlleles().get(l);
                Gene expressed = a.getExpressed();
                Gene parent1 = a.getAlleles()[0];
                Gene parent2 = a.getAlleles()[1];
                values.add(l.getResourcePath());
                values.add(expressed.toString());
                values.add(parent1.toString());
                values.add(parent2.toString());
            }
            return new Object[] { values.toArray() };
        }

        @Callback
        public Object[] getDestInfo(Context context, Arguments args) throws Exception
        {
            IMobGenetics genes = ClonerHelper.getGenes(tileEntity.getStackInSlot(0));
            if (genes == null) throw new Exception("No Genes found in destination slot.");
            List<String> values = Lists.newArrayList();
            for (ResourceLocation l : genes.getAlleles().keySet())
            {
                Alleles a = genes.getAlleles().get(l);
                Gene expressed = a.getExpressed();
                Gene parent1 = a.getAlleles()[0];
                Gene parent2 = a.getAlleles()[1];
                values.add(l.getResourcePath());
                values.add(expressed.toString());
                values.add(parent1.toString());
                values.add(parent2.toString());
            }
            return new Object[] { values.toArray(new String[0]) };
        }

        @Callback
        public Object[] getSelectorInfo(Context context, Arguments args) throws Exception
        {
            ItemStack selector = tileEntity.getStackInSlot(1);
            Set<Class<? extends Gene>> getSelectors = ClonerHelper.getGeneSelectors(selector);
            if (getSelectors.isEmpty()) throw new Exception("No Selector found.");
            List<String> values = Lists.newArrayList();
            for (Class<? extends Gene> geneC : getSelectors)
            {
                try
                {
                    Gene gene = geneC.newInstance();
                    values.add(gene.getKey().getResourcePath());
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                }
            }
            SelectorValue value = ClonerHelper.getSelectorValue(selector);
            values.add(value.toString());
            return new Object[] { values.toArray() };
        }

        @Callback
        public Object[] setSelector(Context context, Arguments args) throws Exception
        {
            ItemStack selector = tileEntity.getStackInSlot(1);
            Set<Class<? extends Gene>> getSelectors = ClonerHelper.getGeneSelectors(selector);
            if (!getSelectors.isEmpty())
                throw new Exception("Cannot set custom selector when a valid one is in the slot.");
            List<String> values = Lists.newArrayList();
            for (int i = 0; i < args.count(); i++)
            {
                values.add(args.checkString(i));
            }
            if (values.isEmpty()) throw new Exception("You need to specify some genes");
            RecipeSplice fixed = new RecipeSplice(true);
            ItemStack newSelector = new ItemStack(Items.WRITTEN_BOOK);
            newSelector.setTagCompound(new NBTTagCompound());
            NBTTagList pages = new NBTTagList();
            for (String s : values)
                pages.appendTag(new NBTTagString(String.format("{\"text\":\"%s\"}", s)));
            newSelector.getTagCompound().setTag("pages", pages);
            SelectorValue value = RecipeSelector.getSelectorValue(tileEntity.getStackInSlot(1));
            newSelector.getTagCompound().setTag(ClonerHelper.SELECTORTAG, value.save());
            newSelector.setStackDisplayName("Selector");
            fixed.setSelector(newSelector);
            tileEntity.setProcess(new PoweredProcess());
            tileEntity.getProcess().setTile(tileEntity);
            tileEntity.getProcess().recipe = fixed;
            tileEntity.getProcess().needed = fixed.getEnergyCost();
            // This is to inform the tile that it should recheck recipe.
            tileEntity.setInventorySlotContents(1, selector);
            return new String[] { "Set" };
        }
    }
}
