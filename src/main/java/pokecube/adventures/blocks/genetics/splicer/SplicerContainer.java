package pokecube.adventures.blocks.genetics.splicer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.core.inventory.BaseContainer;

public class SplicerContainer extends BaseContainer
{
    public static final ContainerType<SplicerContainer> TYPE = new ContainerType<>(SplicerContainer::new);
    private IInventory                                  inv;
    private final IWorldPosCallable                     pos;
    public SplicerTile                                  tile;

    public SplicerContainer(final int id, final PlayerInventory invIn)
    {
        this(id, invIn, IWorldPosCallable.DUMMY);
    }

    public SplicerContainer(final int id, final PlayerInventory invIn, final IWorldPosCallable pos)
    {
        super(SplicerContainer.TYPE, id);
        this.pos = pos;
        this.pos.consume((w, p) ->
        {
            final TileEntity tile = w.getTileEntity(p);
            // Server side
            if (tile instanceof SplicerTile)
            {
                this.tile = (SplicerTile) tile;
                this.inv = this.tile;
            }
        });
        // Client side
        if (this.inv == null)
        {
            this.tile = new SplicerTile();
            this.inv = this.tile;
        }

        this.tile.setCraftMatrix(new PoweredCraftingInventory(this, 1, 3));

        this.trackInt(this.tile.progress);
        this.trackInt(this.tile.total);

        this.addSlot(new Slot(this.tile, this.tile.getOutputSlot(), 114, 35));

        final int di = 17;
        final int di2 = 18;
        final int dj2 = 48;
        final int dj = 32;

        // DNA Container
        this.addSlot(new Slot(this.tile, 0, dj + dj2, di)
        {
            @Override
            public String getSlotTexture()
            {
                if (super.getSlotTexture() == null) super.setBackgroundName("pokecube_adventures:items/slot_dna");
                return super.getSlotTexture();
            }

            @Override
            public boolean isItemValid(final ItemStack stack)
            {
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });
        // Stabiliser
        this.addSlot(new Slot(this.tile, 1, dj + dj2, di + 35)
        {
            @Override
            public String getSlotTexture()
            {
                if (super.getSlotTexture() == null) super.setBackgroundName("pokecube_adventures:items/slot_selector");
                return super.getSlotTexture();
            }

            @Override
            public boolean isItemValid(final ItemStack stack)
            {
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });
        // DNA Source
        this.addSlot(new Slot(this.tile, 2, 47, di + di2)
        {
            @Override
            public String getSlotTexture()
            {
                if (super.getSlotTexture() == null) super.setBackgroundName("pokecube_adventures:items/slot_dna");
                return super.getSlotTexture();
            }

            @Override
            public boolean isItemValid(final ItemStack stack)
            {
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });

        this.bindPlayerInventory(invIn, -19);
    }

    @Override
    public boolean canInteractWith(final PlayerEntity playerIn)
    {
        return true;
    }

    @Override
    public IInventory getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return this.tile.getSizeInventory();
    }

}
