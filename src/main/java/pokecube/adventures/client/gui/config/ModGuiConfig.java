package pokecube.adventures.client.gui.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.commands.Config;
import thut.core.common.config.ConfigBase;

public class ModGuiConfig extends GuiConfig
{
    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, ConfigBase.getConfigElements(Config.instance), PokecubeAdv.ID, false, false,
                GuiConfig.getAbridgedConfigPath(Config.instance.getConfigFile().getAbsolutePath()));
    }
}
