package pokecube.adventures.blocks.afa;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;

public final class BlockAFA extends BlockBase
{
    public BlockAFA()
    {
        super();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityAFA();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn,
            Hand hand, Direction side, float hitX, float hitY, float hitZ)
    {
        if (hand == Hand.MAIN_HAND)
            playerIn.openGui(PokecubeAdv.instance, PokecubeAdv.GUIAFA_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
