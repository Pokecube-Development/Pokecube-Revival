package pokecube.adventures.commands;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;

public class BattleCommand extends CommandBase
{

    @Override
    public String getName()
    {
        return "pokebattle";
    }

    @Override
    public String getUsage(ICommandSource sender)
    {
        return "/pokebattle <player>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSource sender, String[] args) throws CommandException
    {
        if (args.length != 1) { throw new CommandException(getUsage(sender)); }
        final PlayerEntity player = getPlayer(server, sender, args[0]);
        // Use 32 blocks as farthest to look for trainer.
        final int NEAR = 32 * 32;

        List<MobEntity> trainers = player.getEntityWorld().getEntities(MobEntity.class,
                new Predicate<MobEntity>()
                {
                    @Override
                    public boolean apply(MobEntity input)
                    {
                        return input.hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null)
                                && input.getDistanceSq(player) < NEAR;
                    }
                });
        MobEntity target = null;
        int closest = NEAR;
        for (MobEntity e : trainers)
        {
            double d;
            if ((d = e.getDistanceSq(player)) < closest)
            {
                closest = (int) d;
                target = e;
            }
        }
        if (target != null)
        {
            CapabilityHasPokemobs.IHasPokemobs trainer = target.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP,
                    null);
            if (trainer.getTarget() == null) trainer.setTarget(player);
            else throw new CommandException("%s already has a target.", trainer);
            return;
        }

    }

}
