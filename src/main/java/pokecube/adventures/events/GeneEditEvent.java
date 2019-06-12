package pokecube.adventures.events;

import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.adventures.blocks.cloner.ClonerHelper.EditType;
import thut.api.entity.genetics.IMobGenetics;

public class GeneEditEvent extends Event
{
    public final IMobGenetics resultGenes;
    public final EditType     reason;

    public GeneEditEvent(IMobGenetics resultGenes, EditType reason)
    {
        this.resultGenes = resultGenes;
        this.reason = reason;
    }
}
