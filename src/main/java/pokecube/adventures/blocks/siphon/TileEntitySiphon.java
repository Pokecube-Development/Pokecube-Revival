package pokecube.adventures.blocks.siphon;

import java.util.List;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.MobEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.database.PokedexEntry;

public class TileEntitySiphon extends TileEntity implements ITickable
{
    public static int         UPDATERATE        = 20;
    public AxisAlignedBB      box;
    // Caches the mobs nearby, to reduce calls to check for them.
    public List<MobEntity> mobs              = Lists.newArrayList();
    // Time of last check.
    public long               updateTime        = -1;
    public static JEP         parser;
    public int                currentOutput     = 0;
    public int                theoreticalOutput = 0;

    public TileEntitySiphon()
    {
        initParser();
    }

    public TileEntitySiphon(World world)
    {
        this();
    }

    public static int getEnergyGain(int level, int spAtk, int atk, PokedexEntry entry)
    {
        int power = Math.max(atk, spAtk);
        if (parser == null)
        {
            initParser();
        }
        parser.setVarValue("x", level);
        parser.setVarValue("a", power);
        double value = parser.getValue();
        if (Double.isNaN(value))
        {
            initParser();
            parser.setVarValue("x", level);
            parser.setVarValue("a", power);
            value = parser.getValue();
            System.err.println(atk + " " + spAtk + " " + value);
            if (Double.isNaN(value))
            {
                value = 0;
            }
        }
        power = (int) value;
        return Math.max(1, power);
    }

    public static int getMaxEnergy(int level, int spAtk, int atk, PokedexEntry entry)
    {
        return getEnergyGain(level, spAtk, atk, entry);
    }

    private static void initParser()
    {
        parser = new JEP();
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex(); // among other things adds i to the symbol
                             // table
        parser.addVariable("x", 0);
        parser.addVariable("a", 0);
        parser.parseExpression(PokecubeAdv.conf.powerFunction);
    }

    @Override
    public void update()
    {
        if (!world.isRemote) MinecraftForge.EVENT_BUS.post(new SiphonTickEvent(this));
    }

}
