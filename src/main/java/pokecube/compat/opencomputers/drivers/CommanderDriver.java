package pokecube.compat.opencomputers.drivers;

import java.lang.reflect.Constructor;
import java.util.UUID;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import pokecube.adventures.blocks.afa.TileEntityCommander;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import thut.api.maths.Vector3;

public class CommanderDriver extends DriverBase
{
    @Override
    public String getComponentName()
    {
        return "poke_commander";
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side)
    {
        TileEntityCommander pc = (TileEntityCommander) world.getTileEntity(pos);
        return new Environment(pc, getComponentName());
    }

    @Override
    public Class<?> getTileEntityClass()
    {
        return TileEntityCommander.class;
    }
    
    public static final class Environment extends ManagedTileEntityEnvironment<TileEntityCommander>
    {

        public Environment(TileEntityCommander tileEntity, String name)
        {
            super(tileEntity, name);
        }

        @Callback(doc = "function(uuid:string) - gets the uuid of the pokemob to command.")
        public Object[] getPokeID(Context context, Arguments args) throws Exception
        {
            return new Object[] { tileEntity.pokeID };
        }

        @Callback(doc = "function(uuid:string) - Sets the uuid of the pokemob to command.")
        public Object[] setPokeID(Context context, Arguments args) throws Exception
        {
            String var = args.checkString(0);
            tileEntity.pokeID = UUID.fromString(var);
            return new Object[] { true, tileEntity.pokeID };
        }

        @Callback(doc = "function(command:string, args...) - Sets the command and the arguments for it to run, positions are relative to the controller")
        public Object[] setCommand(Context context, Arguments args) throws Exception
        {
            Command command = Command.valueOf(args.checkString(0));
            Object[] commandArgs = getArgs(command, args);
            tileEntity.setCommand(command, commandArgs);
            return new Object[] { true, command };
        }

        @Callback(doc = "function() - Executes the set command, setCommand must be called beforehand.")
        public Object[] executeCommand(Context context, Arguments args) throws Exception
        {
            tileEntity.sendCommand();
            return new Object[] { true, tileEntity.command };
        }

        @Callback(doc = "function() - Gets the moves known by the pokemob.")
        public Object[] getMoves(Context context, Arguments args) throws Exception
        {
            if (tileEntity.pokeID == null) throw new Exception("No Pokemob set");
            WorldServer world = (WorldServer) tileEntity.getWorld();
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(tileEntity.pokeID));
            if (pokemob == null) throw new Exception("No Pokemob found for set ID");
            return pokemob.getMoves();
        }

        @Callback(doc = "function() - Gets the current move index for the pokemob.")
        public Object[] getMoveIndex(Context context, Arguments args) throws Exception
        {
            if (tileEntity.pokeID == null) throw new Exception("No Pokemob set");
            WorldServer world = (WorldServer) tileEntity.getWorld();
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(tileEntity.pokeID));
            if (pokemob == null) throw new Exception("No Pokemob found for set ID");
            return new Object[] { pokemob.getMoveIndex() };
        }

        @Callback(doc = "function(index:number) - Sets the current move index for the pokemob.")
        public Object[] setMoveIndex(Context context, Arguments args) throws Exception
        {
            if (tileEntity.pokeID == null) throw new Exception("No Pokemob set");
            WorldServer world = (WorldServer) tileEntity.getWorld();
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(tileEntity.pokeID));
            if (pokemob == null) throw new Exception("No Pokemob found for set ID");
            pokemob.setMoveIndex(args.checkInteger(0));
            return new Object[] { pokemob.getMoveIndex() };
        }

        @Callback(doc = "function(routine:string) - Gets the state of the given routine.")
        public Object[] getRoutineState(Context context, Arguments args) throws Exception
        {
            if (tileEntity.pokeID == null) throw new Exception("No Pokemob set");
            WorldServer world = (WorldServer) tileEntity.getWorld();
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(tileEntity.pokeID));
            if (pokemob == null) throw new Exception("No Pokemob found for set ID");
            AIRoutine routine = AIRoutine.valueOf(args.checkString(0));
            return new Object[] { pokemob.isRoutineEnabled(routine) };
        }

        @Callback(doc = "function(routine:string, state:boolean) - Sets the state of the given routine.")
        public Object[] setRoutineState(Context context, Arguments args) throws Exception
        {
            if (tileEntity.pokeID == null) throw new Exception("No Pokemob set");
            WorldServer world = (WorldServer) tileEntity.getWorld();
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(tileEntity.pokeID));
            if (pokemob == null) throw new Exception("No Pokemob found for set ID");
            AIRoutine routine = AIRoutine.valueOf(args.checkString(0));
            pokemob.setRoutineState(routine, args.checkBoolean(1));
            return new Object[] { true, pokemob.isRoutineEnabled(routine) };
        }

        @Callback(doc = "function() - Gets the home location for the pokemob.")
        public Object[] getHome(Context context, Arguments args) throws Exception
        {
            if (tileEntity.pokeID == null) throw new Exception("No Pokemob set");
            WorldServer world = (WorldServer) tileEntity.getWorld();
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(tileEntity.pokeID));
            if (pokemob == null) throw new Exception("No Pokemob found for set ID");
            return new Object[] { pokemob.getHome() };
        }

        @Callback(doc = "function(x:number, y:number, z:number, d:homeDistance) - Sets home location, relative to the controller.")
        public Object[] setHome(Context context, Arguments args) throws Exception
        {
            if (tileEntity.pokeID == null) throw new Exception("No Pokemob set");
            WorldServer world = (WorldServer) tileEntity.getWorld();
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(tileEntity.pokeID));
            if (pokemob == null) throw new Exception("No Pokemob found for set ID");
            pokemob.setHome(args.checkInteger(0) + tileEntity.getPos().getX(), args.checkInteger(1) + tileEntity.getPos().getY(),
                    args.checkInteger(2) + tileEntity.getPos().getZ(), 16);
            return new Object[] { true };
        }

        private Object[] getArgs(Command command, Arguments args) throws Exception
        {
            Class<? extends IMobCommandHandler> clazz = IHasCommands.COMMANDHANDLERS.get(command);
            for (Constructor<?> c : clazz.getConstructors())
            {
                if (c.getParameterCount() != 0) { return getArgs(c, args); }
            }
            return null;
        }

        private Object[] getArgs(Constructor<?> constructor, Arguments args) throws Exception
        {
            Class<?>[] argTypes = constructor.getParameterTypes();
            int index = 1;
            Object[] ret = new Object[argTypes.length];
            for (int i = 0; i < ret.length; i++)
            {
                Class<?> type = argTypes[i];
                if (type == Vector3.class)
                {
                    Vector3 arg = Vector3.getNewVector();
                    arg.set(args.checkDouble(index) + tileEntity.getPos().getX(), args.checkDouble(index + 1) + tileEntity.getPos().getY(),
                            args.checkDouble(index + 2) + tileEntity.getPos().getZ());
                    index += 3;
                    ret[i] = arg;
                }
                else if (type == float.class || type == Float.class)
                {
                    float arg = (float) args.checkDouble(index);
                    index += 1;
                    ret[i] = arg;
                }
                else if (type == byte.class || type == Byte.class)
                {
                    byte arg = (byte) args.checkInteger(index);
                    index += 1;
                    ret[i] = arg;
                }
                else if (type == int.class || type == Integer.class)
                {
                    int arg = args.checkInteger(index);
                    index += 1;
                    ret[i] = arg;
                }
                else if (type == boolean.class || type == Boolean.class)
                {
                    boolean arg = args.checkBoolean(index);
                    index += 1;
                    ret[i] = arg;
                }
                else if (type == String.class)
                {
                    String arg = args.checkString(index);
                    index += 1;
                    ret[i] = arg;
                }
            }
            return ret;
        }

    }
}
