package pokecube.adventures.blocks.genetics.helper;

import net.minecraft.inventory.container.ContainerType;
import pokecube.core.inventory.BaseContainer;

public abstract class PoweredContainer extends BaseContainer
{
    protected PoweredContainer(final ContainerType<?> type, final int id)
    {
        super(type, id);
    }

}
