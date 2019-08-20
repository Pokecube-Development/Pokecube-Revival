package pokecube.adventures.blocks.warppad;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.api.maths.Vector4;

public class WarppadTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(WarppadTile::new,
            PokecubeAdv.WARPPAD).build(null);

    public static void warp(final Entity entityIn, final TeleDest dest)
    {
        System.out.println(entityIn + " " + dest);
    }

    public TeleDest dest   = null;
    public int      energy = 0;

    public WarppadTile()
    {
        super(WarppadTile.TYPE);
    }

    public WarppadTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public void onWalkedOn(final Entity entityIn)
    {
        if (this.dest == null) this.dest = new TeleDest(new Vector4(this.getPos().getX(), this.getPos().getY() + 4, this
                .getPos().getZ(), this.world.dimension.getType().getId()));
        if (this.dest != null) WarppadTile.warp(entityIn, this.dest);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        if (compound.contains("dest"))
        {
            final CompoundNBT tag = compound.getCompound("dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        super.read(compound);
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        if (this.dest != null)
        {
            final CompoundNBT tag = new CompoundNBT();
            this.dest.writeToNBT(tag);
            compound.put("dest", tag);
        }
        return super.write(compound);
    }
}
