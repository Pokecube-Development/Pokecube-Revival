package pokecube.adventures.entity.trainers;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.items.ItemBadge;
import thut.api.maths.Vector3;

public class EntityLeader extends EntityTrainer
{
    private boolean randomBadge = false;

    public EntityLeader(World world)
    {
        super(world);
        aiStates.setAIState(IHasNPCAIStates.STATIONARY, true);
        aiStates.setAIState(IHasNPCAIStates.TRADES, false);
        pokemobsCap.resetTime = 0;
    }

    public EntityLeader(World world, TypeTrainer type, int maxXp, Vector3 location)
    {
        this(world, location);
        if (!world.isRemote) initTrainer(type, maxXp);
    }

    public EntityLeader(World par1World, Vector3 location)
    {
        super(par1World);
        aiStates.setAIState(IHasNPCAIStates.TRADES, false);

        this.setSize(0.6F, 1.8F);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, PlayerEntity.class, 3.0F, 1.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, MobEntity.class, 8.0F));
        if (location != null)
        {
            location.moveEntity(this);
            setStationary(location);
        }
    }

    @Override
    public void initTrainer(TypeTrainer type, int level)
    {
        super.initTrainer(type, level);
        aiStates.setAIState(IHasNPCAIStates.TRADES, false);
    }

    @Override
    public boolean processInteract(PlayerEntity player, Hand hand, ItemStack stack)
    {
        if (!player.capabilities.isCreativeMode || hand == Hand.OFF_HAND) return false;

        if (ItemBadge.isBadge(player.getHeldItem(hand)))
        {
            if (!rewardsCap.getRewards().isEmpty())
                rewardsCap.getRewards().set(0, new Reward(player.getHeldItem(hand).copy()));
            else rewardsCap.getRewards().add(new Reward(player.getHeldItem(hand).copy()));
            if (!getEntityWorld().isRemote) player
                    .sendMessage(new StringTextComponent("Badge set to " + player.getHeldItem(hand).getDisplayName()));
            this.setHeldItem(Hand.OFF_HAND, rewardsCap.getRewards().get(0).stack);
        }
        return super.processInteract(player, hand, stack);
    }

    @Override
    public void writeEntityToNBT(CompoundNBT nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.putBoolean("randomBadge", randomBadge);
    }

    @Override
    public void readEntityFromNBT(CompoundNBT nbt)
    {
        super.readEntityFromNBT(nbt);
        randomBadge = nbt.getBoolean("randomBadge");
    }

    public boolean randomBadge()
    {
        return randomBadge;
    }
}
