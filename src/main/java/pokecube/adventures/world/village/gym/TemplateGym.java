package pokecube.adventures.world.village.gym;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplateStructure;

public class TemplateGym extends TemplateStructure
{
    public static final String GYM_GENERAL = "gym_general";

    public TemplateGym()
    {
        super();
        setOffset(-2);
    }

    public TemplateGym(BlockPos pos, Direction dir)
    {
        super(GYM_GENERAL, pos, dir);
    }

    @Override
    public Template getTemplate()
    {
        if (template != null) return template;
        return template = PokecubeTemplates.getTemplate(GYM_GENERAL);
    }
}
