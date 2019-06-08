package pokecube.adventures.blocks.commander;

import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.logging.Level;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import thut.api.maths.Vector3;

public class TileEntityCommander extends TileEntityOwnable
{
    protected boolean          addedToNetwork = false;
    public UUID                pokeID         = null;
    public Command             command        = null;
    private IMobCommandHandler handler        = null;
    public String              args           = "";
    protected int              power          = 0;

    public TileEntityCommander()
    {
        super();
    }

    public void setCommand(Command command, String args) throws Exception
    {
        this.command = command;
        this.args = args;
        if (command != null) initCommand();
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new StringTextComponent("Pokemob Commander");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        return bb;
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        CompoundNBT CompoundNBT = new CompoundNBT();
        if (getWorld().isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, CompoundNBT);
        this.writeToNBT(CompoundNBT);
        return new SPacketUpdateTileEntity(this.getPos(), 3, CompoundNBT);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT nbt = new CompoundNBT();
        return writeToNBT(nbt);
    }

    /** Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible
     * for sending the packet.
     *
     * @param net
     *            The NetworkManager the packet originated from
     * @param pkt
     *            The data packet */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        if (getWorld().isRemote)
        {
            CompoundNBT nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    @Override
    public void readFromNBT(CompoundNBT nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("pokeIDMost")) pokeID = nbt.getUniqueId("pokeID");
        if (nbt.hasKey("cmd")) this.command = Command.valueOf(nbt.getString("cmd"));
        this.args = nbt.getString("args");
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT nbt)
    {
        super.writeToNBT(nbt);
        if (getPokeID() != null) nbt.setUniqueId("pokeID", getPokeID());
        nbt.putString("args", args);
        if (this.command != null) nbt.putString("cmd", this.command.name());
        return nbt;
    }

    public UUID getPokeID()
    {
        return pokeID;
    }

    public void setPokeID(UUID pokeID)
    {
        this.pokeID = pokeID;
    }

    public Command getCommand()
    {
        return command;
    }

    protected void initCommand() throws Exception
    {
        setCommand(command, getArgs());
    }

    private Object[] getArgs() throws Exception
    {
        Class<? extends IMobCommandHandler> clazz = IHasCommands.COMMANDHANDLERS.get(command);
        for (Constructor<?> c : clazz.getConstructors())
        {
            if (c.getParameterCount() != 0) { return getArgs(c); }
        }
        return null;
    }

    private Object[] getArgs(Constructor<?> constructor)
    {
        String[] args = this.args.split(",");
        Class<?>[] argTypes = constructor.getParameterTypes();
        int index = 0;
        Object[] ret = new Object[argTypes.length];
        for (int i = 0; i < ret.length; i++)
        {
            Class<?> type = argTypes[i];
            if (type == Vector3.class)
            {
                Vector3 arg = Vector3.getNewVector();
                arg.set(Double.parseDouble(args[index]) + getPos().getX(),
                        Double.parseDouble(args[index + 1]) + getPos().getY(),
                        Double.parseDouble(args[index + 2]) + getPos().getZ());
                index += 3;
                ret[i] = arg;
            }
            else if (type == float.class || type == Float.class)
            {
                float arg = (float) Double.parseDouble(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == byte.class || type == Byte.class)
            {
                byte arg = (byte) Integer.parseInt(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == int.class || type == Integer.class)
            {
                int arg = Integer.parseInt(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == boolean.class || type == Boolean.class)
            {
                boolean arg = Boolean.parseBoolean(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == String.class)
            {
                String arg = args[index];
                index += 1;
                ret[i] = arg;
            }
        }
        return ret;
    }

    public void setCommand(Command command, Object... args) throws Exception
    {
        this.command = command;
        Class<? extends IMobCommandHandler> clazz = IHasCommands.COMMANDHANDLERS.get(command);
        if (args == null)
        {
            handler = clazz.newInstance();
            return;
        }
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++)
        {
            argTypes[i] = args[i].getClass();
        }
        Constructor<? extends IMobCommandHandler> constructor = clazz.getConstructor(argTypes);
        handler = constructor.newInstance(args);
    }

    public void sendCommand() throws Exception
    {
        World w = getWorld();
        if (!(w instanceof WorldServer)) return;
        if (this.command != null && handler == null) initCommand();
        if (handler == null) throw new Exception("No CommandHandler has been set");
        if (pokeID == null) throw new Exception("No Pokemob Set, please set a UUID first.");
        WorldServer world = (WorldServer) w;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new Exception("Pokemob for given ID is not found.");
        try
        {
            handler.handleCommand(pokemob);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.SEVERE, "Error executing a command for a pokemob", e);
            throw new Exception("Error handling the command", e);
        }
    }
}
