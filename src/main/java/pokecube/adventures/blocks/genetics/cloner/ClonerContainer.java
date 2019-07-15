package pokecube.adventures.blocks.genetics.cloner;

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

public class ClonerContainer extends BaseContainer
{
    public static final ContainerType<ClonerContainer> TYPE = new ContainerType<>(ClonerContainer::new);
    private IInventory                                 inv;
    private final IWorldPosCallable                    pos;
    public ClonerTile                                  tile;

    public ClonerContainer(final int id, final PlayerInventory invIn)
    {
        this(id, invIn, IWorldPosCallable.DUMMY);
    }

    public ClonerContainer(final int id, final PlayerInventory invIn, final IWorldPosCallable pos)
    {
        super(ClonerContainer.TYPE, id);
        this.pos = pos;
        this.pos.consume((w, p) ->
        {
            final TileEntity tile = w.getTileEntity(p);
            // Server side
            if (tile instanceof ClonerTile)
            {
                this.tile = (ClonerTile) tile;
                this.inv = this.tile;
            }
        });
        // Client side
        if (this.inv == null)
        {
            this.tile = new ClonerTile();
            this.inv = this.tile;
        }

        this.tile.setCraftMatrix(new PoweredCraftingInventory(this, 3, 3));

        this.trackInt(this.tile.progress);
        this.trackInt(this.tile.total);

        this.addSlot(new Slot(this.tile, this.tile.getOutputSlot(), 124, 35));

        final int di = 17;
        final int di2 = 9;
        final int dj = 32;

        int i = 0;
        int j = 0;
        this.addSlot(new Slot(this.inv, 0, dj - 21 + j * 18, di + i * 18)
        {
            @Override
            public String getSlotTexture()
            {
                if (super.getSlotTexture() == null) super.setBackgroundName("pokecube_adventures:items/slot_bottle");
                return super.getSlotTexture();
            }

            @Override
            public boolean isItemValid(final ItemStack stack)
            {
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });
        i = 2;
        this.addSlot(new Slot(this.inv, 1, dj - 21 + j * 18, di + i * 18)
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

        i = 0;
        this.addSlot(new Slot(this.inv, 2, dj + j * 18, di + di2 + i * 18));
        i = 1;
        this.addSlot(new Slot(this.inv, 3, dj + j * 18, di + di2 + i * 18));

        i = 0;
        j = 1;
        this.addSlot(new Slot(this.inv, 5, dj + j * 18, di + i * 18));
        i = 1;
        this.addSlot(new Slot(this.inv, 6, dj + j * 18, di + i * 18));
        i = 2;
        this.addSlot(new Slot(this.inv, 7, dj + j * 18, di + i * 18));

        j = 2;
        i = 0;
        this.addSlot(new Slot(this.inv, 8, dj + j * 18, di + di2 + i * 18));
        i = 1;
        this.addSlot(new Slot(this.inv, 4, dj + j * 18, di + di2 + i * 18));

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
