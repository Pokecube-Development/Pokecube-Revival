package pokecube.adventures.blocks.genetics.helper.recipe;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;

public interface IPoweredProgress extends IInventory
{
    int addEnergy(int energy, boolean simulate);

    PoweredCraftingInventory getCraftMatrix();

    int getOutputSlot();

    PoweredProcess getProcess();

    PlayerEntity getUser();

    boolean isValid(Class<? extends PoweredRecipe> recipe);

    void setCraftMatrix(PoweredCraftingInventory matrix);

    void setProcess(PoweredProcess process);

    void setProgress(int progress);
}
