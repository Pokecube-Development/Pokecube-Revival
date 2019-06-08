package pokecube.adventures.ai.tasks;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class AIFindTarget extends AITrainerBase implements ITargetWatcher
{
    // The entity (normally a player) that is the target of this trainer.
    final Class<? extends LivingEntity>[] targetClass;
    // Predicated to return true for invalid targets
    final Predicate<LivingEntity>         validTargets;

    private float                             agroChance = 1f;

    @SafeVarargs
    public AIFindTarget(LivingEntity entityIn, float agressionProbability,
            Class<? extends LivingEntity>... targetClass)
    {
        super(entityIn);
        this.trainer.addTargetWatcher(this);
        this.targetClass = targetClass;
        validTargets = new Predicate<LivingEntity>()
        {
            private boolean validClass(LivingEntity input)
            {
                for (Class<? extends LivingEntity> s : targetClass)
                {
                    if (s.isInstance(input)) return true;
                }
                return false;
            }

            @Override
            public boolean apply(LivingEntity input)
            {
                // If the input has attacked us recently, then return true
                // regardless of following checks.
                if (input.getLastAttackedEntity() == entity
                        && input.ticksExisted - input.getLastAttackedEntityTime() < 50)
                    return true;
                // Only target valid classes.
                if (!validClass(input) || !input.attackable()) return false;
                // Don't target pets
                if (input instanceof IEntityOwnable && ((IEntityOwnable) input).getOwner() == entityIn) return false;
                // Don't target invulnerable players (spectator/creative)
                if (input instanceof PlayerEntity
                        && (((PlayerEntity) input).capabilities.isCreativeMode || ((PlayerEntity) input).isSpectator()))
                    return false;
                // Return true if player can battle the input.
                return trainer.canBattle(input);
            }
        };
        agroChance = agressionProbability;
    }

    @SafeVarargs
    public AIFindTarget(LivingEntity entityIn, Class<? extends LivingEntity>... targetClass)
    {
        this(entityIn, 1, targetClass);
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        if (aiTracker != null && aiTracker.getAIState(IHasNPCAIStates.FIXEDDIRECTION) && trainer.getTarget() == null)
        {
            entity.setRotationYawHead(aiTracker.getDirection());
            entity.prevRotationYawHead = aiTracker.getDirection();
            entity.rotationYawHead = aiTracker.getDirection();
            entity.rotationYaw = aiTracker.getDirection();
            entity.prevRotationYaw = aiTracker.getDirection();
        }
        if (shouldExecute()) updateTask();
    }

    public boolean shouldExecute()
    {
        if (trainer.getTarget() != null)
        { // Check if target is invalid.
            if (trainer.getTarget() != null && trainer.getTarget().isDead)
            {
                trainer.setTarget(null);
                trainer.resetPokemob();
                return false;
            }
            return false;
        }
        // Dead trainers can't fight.
        if (!entity.isEntityAlive() || entity.ticksExisted % 20 != 0) return false;
        // Permfriendly trainers shouldn't fight.
        if (aiTracker != null && aiTracker.getAIState(IHasNPCAIStates.PERMFRIENDLY)) return false;
        // Trainers on cooldown shouldn't fight, neither should friendly ones
        if (trainer.getCooldown() > entity.getEntityWorld().getGameTime()
                || !trainer.isAgressive()) { return false; }
        return true;
    }

    public void updateTask()
    {
        // If target is valid, return.
        if (trainer.getTarget() != null) return;

        // Check random chance of actually aquiring a target.
        if (Math.random() > agroChance) return;

        // Look for targets
        Vector3 here = Vector3.getNewVector().set(entity);
        LivingEntity target = null;
        int sight = trainer.getAgressDistance();
        targetTrack:
        {
            here.addTo(0, entity.getEyeHeight(), 0);
            Vector3 look = Vector3.getNewVector().set(entity.getLook(1));
            here.addTo(look);
            look.scalarMultBy(sight);
            look.addTo(here);
            List<LivingEntity> targets = MovesUtils.targetsHit(entity, look);
            if (!targets.isEmpty()) for (Object o : targets)
            {
                LivingEntity e = (LivingEntity) o;
                double dist = e.getDistance(entity);
                // Only visible or valid targets.
                if (validTargetSet(e) && dist < sight)
                {
                    target = e;
                    break targetTrack;
                }
            }
        }

        // If no target, return false.
        if (target == null)
        {
            // If trainer was in battle (any of these 3) reset trainer before
            // returning.
            if (trainer.getOutMob() != null || aiTracker.getAIState(IHasNPCAIStates.THROWING)
                    || aiTracker.getAIState(IHasNPCAIStates.INBATTLE))
            {
                trainer.resetPokemob();
            }
            return;
        }
        // Set trainers target
        trainer.setTarget(target);
    }

    @Override
    public boolean validTargetSet(LivingEntity target)
    {
        return validTargets.apply(target);
    }
}
