package pokecube.adventures.network.packets;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import pokecube.adventures.blocks.afa.ContainerAFA;
import pokecube.adventures.blocks.daycare.ContainerDaycare;
import pokecube.core.PokecubeCore;
import thut.api.network.PacketHandler;

public class PacketAFA implements IMessage, IMessageHandler<PacketAFA, IMessage>
{
    public CompoundNBT data = new CompoundNBT();

    public PacketAFA()
    {
    }

    @Override
    public IMessage onMessage(final PacketAFA message, final MessageContext ctx)
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

    private void processMessage(MessageContext ctx, PacketAFA message)
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
        IInventory tile = null;
        if ((player.openContainer instanceof ContainerAFA))
        {
            ContainerAFA cont = (ContainerAFA) player.openContainer;
            tile = cont.tile;
        }
        else if ((player.openContainer instanceof ContainerDaycare))
        {
            ContainerDaycare cont = (ContainerDaycare) player.openContainer;
            tile = cont.tile;
        }
        if (tile == null) return;
        if (message.data.hasKey("I"))
        {
            int id = message.data.getInteger("I");
            int val = message.data.getInteger("V");
            tile.setField(id, val);
        }
        PacketHandler.sendTileUpdate((TileEntity) tile);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        try
        {
            data = new PacketBuffer(buf).readCompoundTag();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        new PacketBuffer(buf).writeCompoundTag(data);
    }

}
