package pokecube.adventures.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.items.bags.ContainerBag;
import pokecube.adventures.items.bags.InventoryBag;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;

public class PacketBag implements IMessage, IMessageHandler<PacketBag, IMessage>
{
    public static final byte SETPAGE = 0;
    public static final byte RENAME  = 1;
    public static final byte ONOPEN  = 2;
    public static final byte OPEN    = 3;

    byte                     message;
    public NBTTagCompound    data    = new NBTTagCompound();

    public static void OpenBag(EntityPlayer playerIn)
    {
        InventoryBag inv = InventoryBag.getBag(playerIn);
        PacketBag packet = new PacketBag(PacketBag.ONOPEN);
        packet.data.setInteger("N", inv.boxes.length);
        packet.data.setInteger("S", InventoryBag.PAGECOUNT);
        for (int i = 0; i < inv.boxes.length; i++)
        {
            packet.data.setString("N" + i, inv.boxes[i]);
        }
        PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) playerIn);
        for (int i = 0; i < inv.boxes.length; i++)
        {
            packet = new PacketBag(PacketBag.OPEN);
            packet.data = inv.serializeBox(i);
            PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) playerIn);
        }
        playerIn.openGui(PokecubeAdv.instance, PokecubeAdv.GUIBAG_ID, playerIn.getEntityWorld(),
                InventoryBag.getBag(playerIn).getPage() + 1, 0, 0);
    }

    public PacketBag()
    {
    }

    public PacketBag(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketBag message, final MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(ctx, message);
            }
        });
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        message = buf.readByte();
        PacketBuffer buffer = new PacketBuffer(buf);
        try
        {
            data = buffer.readCompoundTag();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(message);
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeCompoundTag(data);
    }

    void processMessage(MessageContext ctx, PacketBag message)
    {
        EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
        }
        else
        {
            player = ctx.getServerHandler().player;
        }
        ContainerBag container = null;
        if (player.openContainer instanceof ContainerBag) container = (ContainerBag) player.openContainer;
        if (message.message == SETPAGE)
        {
            if (container != null)
            {
                container.gotoInventoryPage(message.data.getInteger("P"));
            }
        }
        if (message.message == RENAME)
        {
            if (container != null)
            {
                String name = message.data.getString("N");
                container.changeName(name);
            }
        }
        if (message.message == OPEN && ctx.side == Side.CLIENT)
        {
            InventoryBag inv = InventoryBag.getBag(player);
            inv.deserializeBox(message.data);
        }
        if (message.message == ONOPEN && ctx.side == Side.CLIENT)
        {
            InventoryBag.blank = new InventoryBag(InventoryBag.blankID);
            InventoryBag bag = InventoryBag.getBag(player);
            int num = message.data.getInteger("N");
            InventoryBag.PAGECOUNT = message.data.getInteger("S");
            ContainerBag.HOLDALL = message.data.getBoolean("A");
            bag.boxes = new String[num];
            for (int i = 0; i < bag.boxes.length; i++)
            {
                bag.boxes[i] = message.data.getString("N" + i);
            }
        }
    }
}
