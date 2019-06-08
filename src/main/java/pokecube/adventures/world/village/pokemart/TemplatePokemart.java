package pokecube.adventures.world.village.pokemart;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplateStructure;

public class TemplatePokemart extends TemplateStructure
{
    public static final String POKEMART = "pokemart";

    public TemplatePokemart()
    {
        super();
        setOffset(-2);
    }

    public TemplatePokemart(BlockPos pos, Direction dir)
    {
        super(POKEMART, pos, dir);
    }

    @Override
    public Template getTemplate()
    {
        if (template != null) return template;
        return template = PokecubeTemplates.getTemplate(POKEMART);
    }
}
