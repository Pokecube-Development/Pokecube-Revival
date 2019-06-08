package pokecube.adventures.ai.helper;

import net.minecraft.entity.MobEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.entity.ai.AIThreadManager.AIStuff;
import thut.api.entity.ai.IAIMob;

public class AIStuffHolder implements IAIMob, ICapabilityProvider
{
    final AIStuff stuff;
    boolean       wrapped = false;

    public AIStuffHolder(MobEntity entity)
    {
        this.stuff = new AIStuff(entity);
    }

    @Override
    public AIStuff getAI()
    {
        return stuff;
    }

    @Override
    public boolean selfManaged()
    {
        return false;
    }

    @Override
    public boolean vanillaWrapped()
    {
        return wrapped;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing)
    {
        return capability == IAIMob.THUTMOBAI;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing)
    {
        return hasCapability(capability, facing) ? IAIMob.THUTMOBAI.cast(this) : null;
    }

    @Override
    public void setWrapped(boolean wrapped)
    {
        this.wrapped = wrapped;
    }

}
