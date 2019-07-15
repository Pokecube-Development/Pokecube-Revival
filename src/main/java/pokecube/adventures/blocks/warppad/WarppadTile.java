package pokecube.adventures.blocks.warppad;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;

public class WarppadTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(WarppadTile::new,
            PokecubeAdv.WARPPAD).build(null);

    public WarppadTile()
    {
        super(WarppadTile.TYPE);
    }

    public WarppadTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
