package pokecube.adventures.blocks.siphon;

public class SiphonTickEvent extends Event
{
    private final TileEntitySiphon tile;

    public SiphonTickEvent(TileEntitySiphon tile)
    {
        this.tile = tile;
    }

    public TileEntitySiphon getTile()
    {
        return tile;
    }

}
