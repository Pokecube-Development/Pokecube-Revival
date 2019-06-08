package pokecube.adventures.network.packets;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
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
    public CompoundNBT    data    = new CompoundNBT();

    public static void OpenBag(PlayerEntity playerIn)
    {
        InventoryBag inv = InventoryBag.getBag(playerIn);
        PacketBag packet = new PacketBag(PacketBag.ONOPEN);
        packet.data.setInteger("N", inv.boxes.length);
        packet.data.setInteger("S", InventoryBag.PAGECOUNT);
        for (int i = 0; i < inv.boxes.length; i++)
        {
            packet.data.putString("N" + i, inv.boxes[i]);
        }
        PokecubeMod.packetPipeline.sendTo(packet, (ServerPlayerEntity) playerIn);
        for (int i = 0; i < inv.boxes.length; i++)
        {
            packet = new PacketBag(PacketBag.OPEN);
            packet.data = inv.serializeBox(i);
            PokecubeMod.packetPipeline.sendTo(packet, (ServerPlayerEntity) playerIn);
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
        PlayerEntity player;
        if (ctx.side == Dist.CLIENT)
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
        if (message.message == OPEN && ctx.side == Dist.CLIENT)
        {
            InventoryBag inv = InventoryBag.getBag(player);
            inv.deserializeBox(message.data);
        }
        if (message.message == ONOPEN && ctx.side == Dist.CLIENT)
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
