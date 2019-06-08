package pokecube.adventures.entity.helper;

import net.minecraft.entity.player.PlayerEntity;

public class Action
{
    final String command;

    public Action(String command)
    {
        this.command = command;
    }

    public void doAction(PlayerEntity target)
    {
        if (command == null || command.trim().isEmpty()) return;
        String[] commands = command.split("``");
        for (String command : commands)
        {
            String editedCommand = command;
            editedCommand = editedCommand.replace("@p", target.getGameProfile().getName());
            editedCommand = editedCommand.replace("'x'", target.posX + "");
            editedCommand = editedCommand.replace("'y'", (target.posY + 1) + "");
            editedCommand = editedCommand.replace("'z'", target.posZ + "");
            target.getServer().getCommandManager().executeCommand(target.getServer(), editedCommand);
        }
    }

    public String getCommand()
    {
        return command;
    }
}
