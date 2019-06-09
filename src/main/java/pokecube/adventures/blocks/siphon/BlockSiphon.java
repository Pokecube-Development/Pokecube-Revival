package pokecube.adventures.blocks.siphon;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import thut.core.common.commands.CommandTools;

public class BlockSiphon extends Block implements ITileEntityProvider
{

    public BlockSiphon()
    {
        super(Material.IRON);
        this.setHardness(10);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata)
    {
        return new TileEntitySiphon(world);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn,
            Hand hand, Direction side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote && hand == Hand.MAIN_HAND)
        {
            TileEntitySiphon tile = (TileEntitySiphon) worldIn.getTileEntity(pos);
            ITextComponent message = CommandTools.makeTranslatedMessage("block.rfsiphon.info", "gold",
                    tile.currentOutput, tile.theoreticalOutput);
            playerIn.sendMessage(message);
        }
        return false;
    }
}
