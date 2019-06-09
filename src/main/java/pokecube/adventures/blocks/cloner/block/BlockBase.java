package pokecube.adventures.blocks.cloner.block;

import java.util.Random;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.core.common.blocks.BlockRotatable;
import thut.lib.CompatWrapper;

public abstract class BlockBase extends BlockRotatable implements ITileEntityProvider
{

    public BlockBase()
    {
        super(Material.IRON);
        this.setLightOpacity(0);
        this.setHardness(10);
        this.setResistance(10);
        this.setLightLevel(1f);
    }

    /** Used to determine ambient occlusion and culling when rebuilding chunks
     * for render */
    @Override
    public boolean isOpaqueCube(BlockState state)
    {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }

    /** Convert the BlockState into the correct metadata value */
    @Override
    public int getMetaFromState(BlockState state)
    {
        int meta = 0;
        int direction;
        switch (state.getValue(FACING))
        {
        case NORTH:
            direction = 0 * 4;
            break;
        case EAST:
            direction = 1 * 4;
            break;
        case SOUTH:
            direction = 2 * 4;
            break;
        case WEST:
            direction = 3 * 4;
            break;
        default:
            direction = 0;
        }
        meta |= direction;
        return meta;
    }

    /** Convert the given metadata into a BlockState for this Block */
    @Override
    public BlockState getStateFromMeta(int meta)
    {
        int direction = meta / 4;
        Direction dir = Direction.NORTH;
        switch (direction)
        {
        case 0:
            dir = Direction.NORTH;
            break;
        case 1:
            dir = Direction.EAST;
            break;
        case 2:
            dir = Direction.SOUTH;
            break;
        case 3:
            dir = Direction.WEST;
            break;
        }
        return this.getDefaultState().withProperty(FACING, dir);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, BlockState state)
    {
        dropItems(world, pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public BlockState getStateForPlacement(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY,
            float hitZ, int meta, LivingEntity placer)
    {
        return this.getStateFromMeta(meta).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public abstract TileEntity createNewTileEntity(World worldIn, int meta);

    private void dropItems(World world, BlockPos pos)
    {
        Random rand = new Random();
        TileEntity tile_entity = world.getTileEntity(pos);

        if (!(tile_entity instanceof IInventory)) { return; }

        IInventory inventory = (IInventory) tile_entity;

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack item = inventory.getStackInSlot(i);
            if (CompatWrapper.isValid(item))
            {
                float rx = rand.nextFloat() * 0.6F + 0.1F;
                float ry = rand.nextFloat() * 0.6F + 0.1F;
                float rz = rand.nextFloat() * 0.6F + 0.1F;
                ItemEntity entity_item = new ItemEntity(world, pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                        new ItemStack(item.getItem(), item.getCount(), item.getItemDamage()));
                if (item.hasTag())
                {
                    entity_item.getItem().setTag(item.getTag().copy());
                }
                float factor = 0.005F;
                entity_item.motionX = rand.nextGaussian() * factor;
                entity_item.motionY = rand.nextGaussian() * factor + 0.2F;
                entity_item.motionZ = rand.nextGaussian() * factor;
                world.spawnEntity(entity_item);
                item.setCount(0);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

}
