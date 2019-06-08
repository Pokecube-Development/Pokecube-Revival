package pokecube.adventures.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import pokecube.adventures.commands.Config;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.events.TrainerSpawnEvent;
import pokecube.core.ai.properties.GuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;

public class TrainerSpawnHandler
{

    private static TrainerSpawnHandler       instance;
    public static Map<UUID, ChunkCoordinate> trainerMap = Maps.newConcurrentMap();
    private static Vector3                   vec1       = Vector3.getNewVector();
    private static Vector3                   vec2       = Vector3.getNewVector();

    /** Adds or updates the location of the trainer.
     * 
     * @param e
     * @return */
    public static void addTrainerCoord(Entity e)
    {
        int x = ((int) e.posX) / 16;
        int y = ((int) e.posY) / 16;
        int z = ((int) e.posZ) / 16;
        int dim = e.dimension;
        ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        trainerMap.put(e.getUniqueID(), coord);
    }

    public static void removeTrainer(Entity e)
    {
        trainerMap.remove(e.getUniqueID());
    }

    public static int countTrainersNear(Entity e, int trainerBox)
    {
        int x = ((int) e.posX) / 16;
        int y = ((int) e.posY) / 16;
        int z = ((int) e.posZ) / 16;
        return countTrainersInArea(e.getEntityWorld(), x, y, z, trainerBox);
    }

    public static int countTrainersInArea(World world, int chunkPosX, int chunkPosY, int chunkPosZ, int trainerBox)
    {
        int tolerance = trainerBox / 16;
        int ret = 0;
        for (ChunkCoordinate o : trainerMap.values())
        {
            ChunkCoordinate coord = o;
            if (chunkPosX >= coord.getX() - tolerance && chunkPosZ >= coord.getZ() - tolerance
                    && chunkPosY >= coord.getY() - tolerance && chunkPosY <= coord.getY() + tolerance
                    && chunkPosX <= coord.getX() + tolerance && chunkPosZ <= coord.getZ() + tolerance
                    && world.dimension.getDimension() == coord.dim)
            {
                ret++;
            }
        }
        return ret;
    }

    /** Given a player, find a random position near it. */
    public static Vector3 getRandomSpawningPointNearEntity(World world, Entity player, int maxRange)
    {
        if (player == null) return null;

        Vector3 v = vec1.set(player);

        Random rand = new Random();

        // SElect random gaussians from here.
        double x = rand.nextGaussian() * maxRange;
        double z = rand.nextGaussian() * maxRange;

        // Cap x and z to distance.
        if (Math.abs(x) > maxRange) x = Math.signum(x) * maxRange;
        if (Math.abs(z) > maxRange) z = Math.signum(z) * maxRange;

        // Don't select distances too far up/down from current.
        double y = Math.min(Math.max(5, rand.nextGaussian() * 10), 10);
        v.addTo(x, y, z);

        // Don't select unloaded areas.
        if (!world.isAreaLoaded(v.getPos(), 8)) return null;

        // Find surface
        Vector3 temp1 = Vector3.getNextSurfacePoint2(world, vec1, vec2.set(Direction.DOWN), 10);

        if (temp1 != null)
        {
            temp1.y++;
            // Check for headroom
            if (!temp1.addTo(0, 1, 0).isClearOfBlocks(world)) return null;
            temp1.y--;
            return temp1;
        }
        return null;
    }

    public static TrainerSpawnHandler getInstance()
    {
        return instance;
    }

    Vector3 v      = Vector3.getNewVector(), v1 = Vector3.getNewVector(), v2 = Vector3.getNewVector();

    JEP     parser = new JEP();

    public TrainerSpawnHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
        instance = this;
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityVillager || event.getObject() instanceof EntityTrainer)
        {
            class Provider extends GuardAICapability implements ICapabilitySerializable<CompoundNBT>
            {
                @Override
                public void deserializeNBT(CompoundNBT nbt)
                {
                    EventsHandler.storage.readNBT(EventsHandler.GUARDAI_CAP, this, null, nbt);
                }

                @Override
                public <T> T getCapability(Capability<T> capability, Direction facing)
                {
                    if (hasCapability(capability, facing)) return EventsHandler.GUARDAI_CAP.cast(this);
                    return null;
                }

                @Override
                public boolean hasCapability(Capability<?> capability, Direction facing)
                {
                    return EventsHandler.GUARDAI_CAP != null && capability == EventsHandler.GUARDAI_CAP;
                }

                @Override
                public CompoundNBT serializeNBT()
                {
                    return (CompoundNBT) EventsHandler.storage.writeNBT(EventsHandler.GUARDAI_CAP, this, null);
                }
            }
            event.addCapability(new ResourceLocation("pokecube_adventures:guardai"), new Provider());
        }
    }

    public void tick(World w)
    {
        if (w.isRemote) { return; }
        if (!SpawnHandler.canSpawnInWorld(w)) return;
        ArrayList<Object> players = new ArrayList<Object>();
        players.addAll(w.playerEntities);
        if (players.size() < 1) return;
        PlayerEntity p = (PlayerEntity) players.get(w.rand.nextInt(players.size()));
        Vector3 v = getRandomSpawningPointNearEntity(w, p, Config.instance.trainerBox);
        if (v == null) return;
        if (v.y < 0) v.y = v.getMaxY(w);
        Vector3 temp = Vector3.getNextSurfacePoint2(w, v, Vector3.secondAxisNeg, 20);
        v = temp != null ? temp.offset(Direction.UP) : v;

        if (!SpawnHandler.checkNoSpawnerInArea(w, v.intX(), v.intY(), v.intZ())) return;
        int count = countTrainersInArea(w, v.intX() / 16, v.intY() / 16, v.intZ() / 16, Config.instance.trainerBox);

        if (count < Config.instance.trainerDensity)
        {
            long time = System.nanoTime();
            EntityTrainer t = getTrainer(v, w);
            if (t == null) return;
            IHasPokemobs cap = CapabilityHasPokemobs.getHasPokemobs(t);
            TrainerSpawnEvent event = new TrainerSpawnEvent(cap.getType(), t, v.getPos(), w);
            if (MinecraftForge.EVENT_BUS.post(event))
            {
                t.setDead();
                return;
            }
            double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 20) PokecubeMod.log(FMLCommonHandler.instance().getEffectiveSide() + " Trainer "
                    + cap.getType().name + " " + dt + "ms ");
            v.offsetBy(Direction.UP).moveEntity(t);
            if (t.pokemobsCap.countPokemon() > 0
                    && SpawnHandler.checkNoSpawnerInArea(w, (int) t.posX, (int) t.posY, (int) t.posZ))
            {
                w.spawnEntity(t);
                addTrainerCoord(t);
            }
            else t.setDead();
        }

    }

    @SubscribeEvent
    public void tickEvent(WorldTickEvent evt)
    {
        if (Config.instance.trainerSpawn && evt.phase == Phase.END && evt.type != Type.CLIENT && evt.side != Dist.CLIENT
                && evt.world.getGameTime() % PokecubeMod.core.getConfig().spawnRate == 0)
        {
            long time = System.nanoTime();
            tick(evt.world);
            double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 50) PokecubeMod
                    .log(FMLCommonHandler.instance().getEffectiveSide() + "Trainer Spawn Tick took " + dt + "ms");
        }
    }

    public EntityTrainer getTrainer(Vector3 v, World w)
    {
        TypeTrainer ttype = null;
        Material m = v.getBlockMaterial(w);
        if (m == Material.AIR && v.offset(Direction.DOWN).getBlockMaterial(w) == Material.AIR)
        {
            v = v.getTopBlockPos(w).offsetBy(Direction.UP);
        }
        SpawnCheck checker = new SpawnCheck(v, w);
        List<TypeTrainer> types = Lists.newArrayList(TypeTrainer.typeMap.values());
        Collections.shuffle(types);
        types:
        for (TypeTrainer type : types)
        {
            for (Entry<SpawnBiomeMatcher, Float> entry : type.matchers.entrySet())
            {
                SpawnBiomeMatcher matcher = entry.getKey();
                Float value = entry.getValue();
                if (w.rand.nextFloat() < value && matcher.matches(checker))
                {
                    ttype = type;
                    break types;
                }
            }
        }
        if (ttype == null) return null;
        int level = SpawnHandler.getSpawnLevel(w, v, Database.getEntry(1));
        EntityTrainer trainer = new EntityTrainer(w, ttype, level);
        trainer.aiStates.setAIState(IHasNPCAIStates.MATES, true);
        trainer.aiStates.setAIState(IHasNPCAIStates.TRADES, true);
        return trainer;
    }
}
