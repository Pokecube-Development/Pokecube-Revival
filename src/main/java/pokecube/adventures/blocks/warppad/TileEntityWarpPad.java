package pokecube.adventures.blocks.warppad;

import org.nfunk.jep.JEP;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.commands.Config;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class TileEntityWarpPad extends TileEntityOwnable
{
    public static JEP parser;

    public static void initParser(String function)
    {
        parser = new JEP();
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex(); // among other things adds i to the symbol
                             // table
        parser.addVariable("dw", 0);
        parser.addVariable("dx", 0);
        parser.addVariable("dy", 0);
        parser.addVariable("dz", 0);
        parser.parseExpression(function);
    }

    public static double MAXRANGE = 64;
    public static int    COOLDOWN = 20;
    public Vector4       link;
    private Vector3      linkPos;
    public Vector3       here;
    boolean              admin    = false;
    boolean              noEnergy = false;
    public int           energy   = 0;

    public TileEntityWarpPad()
    {
    }
    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        CompoundNBT CompoundNBT = new CompoundNBT();
        if (world.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, CompoundNBT);
        this.writeToNBT(CompoundNBT);
        return new SPacketUpdateTileEntity(this.getPos(), 3, CompoundNBT);
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
        if (world.isRemote)
        {
            CompoundNBT nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    public void onStepped(Entity stepper)
    {
        if (world.isRemote || link == null) return;
        if (here == null) here = Vector3.getNewVector().set(this);
        if (linkPos == null)
        {
            linkPos = Vector3.getNewVector().set(link.x, link.y, link.z);
        }

        double distSq = 0;
        long time = world.getGameTime();
        long lastStepped = stepper.getEntityData().getLong("lastWarpPadUse");
        boolean tele = link != null && !link.isEmpty() && lastStepped + COOLDOWN <= time
                && (MAXRANGE < 0 || (distSq = here.distToSq(linkPos)) < MAXRANGE * MAXRANGE);
        if (tele)
        {
            Integer dimHere = stepper.dimension;
            Integer dimThere = (int) link.w;
            if (TeleportHandler.invalidDests.contains(dimHere) || TeleportHandler.invalidDests.contains(dimThere))
            {
                tele = false;
            }
        }

        if (tele && Config.instance.warpPadEnergy && !noEnergy)
        {
            parser.setVarValue("dx", (link.x - here.x));
            parser.setVarValue("dy", (link.y - here.y));
            parser.setVarValue("dz", (link.z - here.z));
            parser.setVarValue("dw", (link.w - getWorld().dimension.getDimension()));
            distSq = parser.getValue();
            tele = energy > distSq;
            if (!tele)
            {
                energy = 0;
                stepper.playSound(SoundEvents.BLOCK_NOTE_BASEDRUM, 1.0F, 1.0F);
            }
            else
            {
                energy -= distSq;
            }
            stepper.getEntityData().putLong("lastWarpPadUse", time);
        }
        if (tele)
        {
            stepper.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
            PacketBuffer buff = new PacketBuffer(Unpooled.buffer());
            buff.writeByte(MessageClient.TELEPORTEFFECTS);
            here.writeToBuff(buff);
            MessageClient packet = new MessageClient(buff);
            PokecubePacketHandler.sendToAllNear(packet, here, stepper.dimension, 20);
            TeleDest d = new TeleDest(link);
            Vector3 loc = d.getLoc();
            int dim = d.getDim();
            if (stepper instanceof PlayerEntity)
            {
                stepper = Transporter.teleportEntity(stepper, loc, dim, false);
            }
            else if (dim == d.getDim())
            {
                stepper.setPositionAndUpdate(loc.x, loc.y, loc.z);
            }
            else
            {
                return;
            }
            if (stepper != null) stepper.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
            buff = new PacketBuffer(Unpooled.buffer());
            buff.writeByte(MessageClient.TELEPORTEFFECTS);
            linkPos.writeToBuff(buff);
            packet = new MessageClient(buff);
            PokecubePacketHandler.sendToAllNear(packet, linkPos, stepper.dimension, 20);
        }
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound)
    {
        super.readFromNBT(tagCompound);
        link = new Vector4(tagCompound.getCompound("link"));
        noEnergy = tagCompound.getBoolean("noEnergy");
        admin = tagCompound.getBoolean("admin");
        energy = tagCompound.getInt("energy");
    }

    public int receiveEnergy(Direction facing, int maxReceive, boolean simulate)
    {
        int receive = Math.min(maxReceive, PokecubeAdv.conf.warpPadMaxEnergy - energy);
        if (!simulate && receive > 0)
        {
            energy += receive;
        }
        return receive;
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound)
    {
        super.writeToNBT(tagCompound);
        if (link != null)
        {
            CompoundNBT linkTag = new CompoundNBT();
            link.writeToNBT(linkTag);
            tagCompound.setTag("link", linkTag);
        }
        tagCompound.putBoolean("noEnergy", noEnergy);
        tagCompound.putBoolean("admin", admin);
        tagCompound.setInteger("energy", energy);
        return tagCompound;
    }
}
