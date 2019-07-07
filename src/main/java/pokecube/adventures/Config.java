package pokecube.adventures;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.LivingEntity;
import thut.core.common.config.Config.ConfigData;

public class Config extends ConfigData
{
    public static final Config instance = new Config();

    public List<Class<? extends LivingEntity>> customTrainers = Lists.newArrayList();

    public boolean npcsAreTrainers = true;

    public int trainerCooldown = 5000;

    public int trainerSightRange = 8;

    public int trainerBattleDelay = 5000;

    public int trainerSendOutDelay = 50;

    public boolean trainerslevel = true;

    public boolean trainerSpawn = true;

    public int trainerBox = 64;

    public double trainerDensity = 2;

    public boolean pokemobsHarmNPCs = false;

    public boolean trainersBattleEachOther = true;

    public boolean trainersBattlePokemobs = true;

    public int trainerDeAgressTicks = 100;

    public boolean trainersMate = true;

    public String defaultReward = "minecraft:emerald";

    public int fossilReanimateCost = 50000;

    public boolean anyReanimate = true;

    public Config()
    {
        super(PokecubeAdv.ID);
    }

    @Override
    public void onUpdated()
    {
        // TODO Auto-generated method stub

    }

}
