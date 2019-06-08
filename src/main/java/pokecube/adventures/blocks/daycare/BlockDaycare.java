package pokecube.adventures.blocks.daycare;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.BlockBase;

public final class BlockDaycare extends BlockBase
{
    public BlockDaycare()
    {
        super();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityDaycare();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, PlayerEntity playerIn,
            Hand hand, Direction side, float hitX, float hitY, float hitZ)
    {
        if (hand == Hand.MAIN_HAND)
            playerIn.openGui(PokecubeAdv.instance, PokecubeAdv.GUIAFA_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
