package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.IMobGenetics;

public class RecipeFossilRevive extends PoweredRecipe
{

    public static class AnyMatcher implements ReviveMatcher
    {
        public static int level = 20;

        @Override
        public boolean complete(final IPoweredProgress tile)
        {
            final World world = ((TileEntity) tile).getWorld();
            final BlockPos pos = ((TileEntity) tile).getPos();
            final PokedexEntry entry = this.getEntry(tile.getCraftMatrix());
            if (entry == Database.missingno) return false;
            final boolean tame = !entry.legendary;
            MobEntity entity = PokecubeCore.createPokemob(entry, world);
            if (entity != null)
            {
                ItemStack dnaSource = tile.getStackInSlot(0);
                if (!dnaSource.isEmpty()) dnaSource = dnaSource.copy();
                IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
                entity.setHealth(entity.getMaxHealth());
                // to avoid the death on spawn
                final int exp = Tools.levelToXp(entry.getEvolutionMode(), AnyMatcher.level);
                // that will make your pokemob around level 3-5.
                // You can give him more XP if you want
                entity = (pokemob = pokemob.setForSpawn(exp)).getEntity();
                if (tile.getUser() != null && tame) pokemob.setOwner(tile.getUser().getUniqueID());
                final Direction dir = world.getBlockState(pos).get(HorizontalBlock.HORIZONTAL_FACING);
                entity.setLocationAndAngles(pos.getX() + 0.5 + dir.getXOffset(), pos.getY() + 1, pos.getZ() + 0.5 + dir
                        .getZOffset(), world.rand.nextFloat() * 360F, 0.0F);
                entity.getEntityData().putBoolean("cloned", true);
                world.addEntity(entity);
                final IMobGenetics genes = ClonerHelper.getGenes(dnaSource);
                if (genes != null) GeneticsManager.initFromGenes(genes, pokemob);
                entity.playAmbientSound();
            }
            return true;
        }

        @Override
        public PokedexEntry getEntry(final CraftingInventory inventory)
        {
            final ItemStack dnaSource = inventory.getStackInSlot(0);
            if (dnaSource.isEmpty()) return Database.missingno;
            final PokedexEntry entry = ClonerHelper.getFromGenes(dnaSource);
            if (entry == null) return Database.missingno;
            return entry;
        }

    }

    public static interface ReviveMatcher
    {
        boolean complete(final IPoweredProgress tile);

        PokedexEntry getEntry(CraftingInventory inventory);

        default PokedexEntry getEntry(final CraftingInventory inventory, final World world)
        {
            return this.getEntry(inventory);
        }

        default ItemStack shouldKeep(final CraftingInventory inventory, final int index)
        {
            return net.minecraftforge.common.ForgeHooks.getContainerItem(inventory.getStackInSlot(index));
        }
    }

    public static int                                         ENERGYCOST = 10000;
    public static final IRecipeSerializer<RecipeFossilRevive> SERIALIZER = IRecipeSerializer.register(
            "pokecube_adventures:reviving", new SpecialRecipeSerializer<>(RecipeFossilRevive::new));

    public static Function<ItemStack, Integer> ENERGYNEED = (s) -> RecipeFossilRevive.ENERGYCOST;
    private static List<RecipeFossilRevive>    recipeList = Lists.newArrayList();

    private static HashMap<PokedexEntry, RecipeFossilRevive> entryMap = Maps.newHashMap();

    public static ReviveMatcher             ANYMATCHER;
    public static final List<ReviveMatcher> MATCHERS = Lists.newArrayList();

    public static RecipeFossilRevive getRecipe(final PokedexEntry entry)
    {
        return RecipeFossilRevive.entryMap.get(entry);
    }

    public static List<RecipeFossilRevive> getRecipeList()
    {
        return Lists.newArrayList(RecipeFossilRevive.recipeList);
    }

    public RecipeFossilRevive(final ResourceLocation loc)
    {
        super(loc);
    }

    @Override
    public boolean complete(final IPoweredProgress tile)
    {
        return true;
    }

    @Override
    public Function<ItemStack, Integer> getCostFunction()
    {
        return RecipeFossilRevive.ENERGYNEED;
    }

    @Override
    public ItemStack getCraftingResult(final CraftingInventory inv)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public int getEnergyCost()
    {
        return RecipeFossilRevive.ENERGYCOST;
    }

    public PokedexEntry getPokedexEntry(final IPoweredProgress tile)
    {
        PokedexEntry entry = Database.missingno;
        for (final ReviveMatcher matcher : RecipeFossilRevive.MATCHERS)
            if ((entry = matcher.getEntry(tile.getCraftMatrix())) != null) return entry;
        return RecipeFossilRevive.ANYMATCHER.getEntry(tile.getCraftMatrix());
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeFossilRevive.SERIALIZER;
    }

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        for (final ReviveMatcher matcher : RecipeFossilRevive.MATCHERS)
            if (matcher.getEntry(inv, worldIn) != null) return true;
        return RecipeFossilRevive.ANYMATCHER.getEntry(inv, worldIn) != null;
    }
}
