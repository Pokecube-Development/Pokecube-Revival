package pokecube.compat.jei.pokemobs.moves;

import java.util.List;

import javax.annotation.Nonnull;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.config.Constants;
import mezz.jei.startup.ForgeModIdHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import pokecube.compat.jei.JEICompat;

public class PokemobMoveCategory implements IRecipeCategory<PokemobMoveRecipeWrapper>
{
    private static final int          craftOutputSlot = 0;
    private static final int          craftInputSlot1 = 1;

    public static final int           width           = 116;
    public static final int           height          = 54;

    @Nonnull
    private final IDrawable           background;
    @Nonnull
    private final IDrawable           icon;
    @Nonnull
    private final String              localizedName;
    private final ICraftingGridHelper craftingGridHelper;

    public PokemobMoveCategory(IGuiHelper guiHelper)
    {
        ResourceLocation location = Constants.RECIPE_GUI_VANILLA;
        background = guiHelper.createDrawable(location, 0, 60, width, height);
        localizedName = Translator.translateToLocal("gui.jei.pokemobs.moves");
        icon = guiHelper.createDrawable(JEICompat.TABS, 0, 0, 16, 16);
        craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
    }

    @Override
    public String getUid()
    {
        return JEICompat.POKEMOBMOVES;
    }

    @Override
    @Nonnull
    public IDrawable getBackground()
    {
        return background;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return localizedName;
    }

    @Override
    public IDrawable getIcon()
    {
        return icon;
    }

    @Override
    public void drawExtras(Minecraft minecraft)
    {
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PokemobMoveRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(craftOutputSlot, false, 94, 18);

        for (int y = 0; y < 3; ++y)
        {
            for (int x = 0; x < 3; ++x)
            {
                int index = craftInputSlot1 + x + (y * 3);
                guiItemStacks.init(index, true, x * 18, y * 18);
            }
        }

        if (recipeWrapper instanceof ICustomCraftingRecipeWrapper)
        {
            ICustomCraftingRecipeWrapper customWrapper = (ICustomCraftingRecipeWrapper) recipeWrapper;
            customWrapper.setRecipe(recipeLayout, ingredients);
            return;
        }

        List<List<ItemStack>> inputs = ingredients.getInputs(ItemStack.class);
        List<List<ItemStack>> outputs = ingredients.getOutputs(ItemStack.class);

        if (recipeWrapper instanceof IShapedCraftingRecipeWrapper)
        {
            IShapedCraftingRecipeWrapper wrapper = (IShapedCraftingRecipeWrapper) recipeWrapper;
            craftingGridHelper.setInputs(guiItemStacks, inputs, wrapper.getWidth(), wrapper.getHeight());
        }
        else
        {
            craftingGridHelper.setInputs(guiItemStacks, inputs);
            recipeLayout.setShapeless();
        }
        guiItemStacks.set(craftOutputSlot, outputs.get(0));

        if (recipeWrapper instanceof ICraftingRecipeWrapper)
        {
            ICraftingRecipeWrapper craftingRecipeWrapper = (ICraftingRecipeWrapper) recipeWrapper;
            ResourceLocation registryName = craftingRecipeWrapper.getRegistryName();
            if (registryName != null)
            {
                guiItemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) ->
                {
                    if (slotIndex == craftOutputSlot)
                    {
                        String recipeModId = registryName.getResourceDomain();

                        boolean modIdDifferent = false;
                        ResourceLocation itemRegistryName = ingredient.getItem().getRegistryName();
                        if (itemRegistryName != null)
                        {
                            String itemModId = itemRegistryName.getResourceDomain();
                            modIdDifferent = !recipeModId.equals(itemModId);
                        }

                        if (modIdDifferent)
                        {
                            String modName = ForgeModIdHelper.getInstance().getFormattedModNameForModId(recipeModId);
                            tooltip.add(TextFormatting.GRAY
                                    + Translator.translateToLocalFormatted("jei.tooltip.recipe.by", modName));
                        }

                        boolean showAdvanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips
                                || GuiScreen.isShiftKeyDown();
                        if (showAdvanced)
                        {
                            tooltip.add(TextFormatting.GRAY + registryName.getResourcePath());
                        }
                    }
                });
            }
        }
    }

    @Override
    public String getModName()
    {
        return "Pokecube";
    }

}
