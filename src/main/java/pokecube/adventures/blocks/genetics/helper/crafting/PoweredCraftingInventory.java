package pokecube.adventures.blocks.genetics.helper.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;

public class PoweredCraftingInventory extends CraftingInventory
{
    public final Container eventHandler;
    private int            energy = 0;

    public PoweredCraftingInventory(final Container container, final int x, final int y)
    {
        super(container, x, y);
        this.eventHandler = container;
    }

    public int getEnergy()
    {
        return this.energy;
    }

    public void setEnergy(final int in)
    {
        this.energy = in;
    }
}
