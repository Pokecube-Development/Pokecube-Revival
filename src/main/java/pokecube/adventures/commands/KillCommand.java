package pokecube.adventures.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityPokemartSeller;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;

public class KillCommand extends CommandBase
{

    @Override
    public String getName()
    {
        return "trainer_kill";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/trainer_kill <optional|type/all> By default, this doesn't kill leaders, merchants, or trainers marked as invulnerable";
    }

    /** Return the required permission level for this command. */
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        World world = sender.getEntityWorld();
        // TODO check argument for this
        boolean all = false;
        // TODO check argument for this.
        TypeTrainer type = null;
        int num = 0;
        for (Entity mob : world.loadedEntityList)
        {
            if (mob instanceof EntityTrainer)
            {
                EntityTrainer trainer = (EntityTrainer) mob;
                if (trainer.aiStates.getAIState(IHasNPCAIStates.INVULNERABLE)) continue;
                if (all)
                {
                    trainer.setDead();
                    num++;
                    continue;
                }
                if (type != null && trainer.pokemobsCap.getType() == type)
                {
                    trainer.setDead();
                    num++;
                    continue;
                }
                if (!(trainer instanceof EntityLeader || trainer instanceof EntityPokemartSeller))
                {
                    trainer.setDead();
                    num++;
                    continue;
                }
            }
        }
        sender.sendMessage(new TextComponentString("Killed " + num));
    }

}
