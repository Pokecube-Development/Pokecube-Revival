package pokecube.compat.tesla;

import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;

public class ProviderSiphon implements ITeslaProducer, ICapabilityProvider
{
    final TileEntitySiphon tile;

    public ProviderSiphon(TileEntitySiphon tile)
    {
        this.tile = tile;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing)
    {
        return capability == TeslaHandler.TESLA_PRODUCER;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing)
    {
        return (capability == TeslaHandler.TESLA_PRODUCER) ? TeslaHandler.TESLA_PRODUCER.cast(this) : null;
    }

    @Override
    public long takePower(long power, boolean simulated)
    {
        long ret = TeslaHandler.getOutput(tile, power, simulated);
        return ret;
    }

}
