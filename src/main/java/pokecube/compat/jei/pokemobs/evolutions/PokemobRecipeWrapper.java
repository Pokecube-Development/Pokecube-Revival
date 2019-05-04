package pokecube.compat.jei.pokemobs.evolutions;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.MovesUtils;
import thut.api.terrain.BiomeType;

public class PokemobRecipeWrapper implements IRecipeWrapper
{
    final PokemobRecipe recipe;

    public PokemobRecipeWrapper(PokemobRecipe recipe)
    {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
        List<ItemStack> stackInputs = Lists.newArrayList();
        if (recipe.data.item != null) stackInputs.add(recipe.data.item);
        List<PokedexEntry> mobInput = Lists.newArrayList(recipe.data.preEvolution);
        ingredients.setInputs(ItemStack.class, stackInputs);
        ingredients.setInputs(PokedexEntry.class, mobInput);
        ingredients.setOutput(PokedexEntry.class, recipe.data.evolution);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {

    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        List<String> tooltips = Lists.newArrayList();
        Rectangle arrow = new Rectangle(44, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return tooltips;
        EvolutionData data = this.recipe.data;
        String[] messages = data.getEvoString().split("\n");
        // index 0 is just the header..
        for (int i = 1; i < messages.length; i++)
            tooltips.add(messages[i]);
        return tooltips;
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton)
    {
        return false;
    }
}
