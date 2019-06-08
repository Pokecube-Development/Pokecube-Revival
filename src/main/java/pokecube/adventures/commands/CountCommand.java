package pokecube.adventures.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.handlers.TrainerSpawnHandler;

public class CountCommand extends CommandBase
{

    @Override
    public String getName()
    {
        return "trainer_count";
    }

    @Override
    public String getUsage(ICommandSource sender)
    {
        return "/trainer_count";
    }

    /** Return the required permission level for this command. */
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSource sender, String[] args) throws CommandException
    {
        int num = 0;
        if (args.length == 1)
        {
            int range = parseInt(args[0]);
            num = TrainerSpawnHandler.countTrainersNear(sender.getCommandSenderEntity(), range);
        }
        else
        {
            for (World world : server.worlds)
            {
                for (Entity mob : world.loadedEntityList)
                {
                    if (mob instanceof EntityTrainer)
                    {
                        num++;
                    }
                }
            }
        }
        sender.sendMessage(new StringTextComponent("Trainer Count: " + num));
    }

}
