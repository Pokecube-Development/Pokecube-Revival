package pokecube.adventures.network.packets;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityPokemartSeller;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.client.gui.helper.RouteEditHelper;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;

public class PacketTrainer implements IMessage, IMessageHandler<PacketTrainer, IMessage>
{
    public static final String EDITSELF             = "pokecube_adventures.traineredit.self";
    public static final String EDITOTHER            = "pokecube_adventures.traineredit.other";
    public static final String EDITMOB              = "pokecube_adventures.traineredit.mob";
    public static final String EDITTRAINER          = "pokecube_adventures.traineredit.trainer";
    public static final String SPAWNTRAINER         = "pokecube_adventures.traineredit.spawn";

    public static final byte   MESSAGEUPDATETRAINER = 0;
    public static final byte   MESSAGENOTIFYDEFEAT  = 1;
    public static final byte   MESSAGEKILLTRAINER   = 2;
    public static final byte   MESSAGEUPDATEMOB     = 3;
    public static final byte   MESSAGESPAWNTRAINER  = 4;

    public static void register()
    {
        PermissionAPI.registerNode(EDITSELF, DefaultPermissionLevel.OP, "Allowed to edit self with trainer editor");
        PermissionAPI.registerNode(EDITOTHER, DefaultPermissionLevel.OP,
                "Allowed to edit other player with trainer editor");
        PermissionAPI.registerNode(EDITMOB, DefaultPermissionLevel.OP, "Allowed to edit pokemobs with trainer editor");
        PermissionAPI.registerNode(EDITTRAINER, DefaultPermissionLevel.OP,
                "Allowed to edit trainer with trainer editor");
        PermissionAPI.registerNode(SPAWNTRAINER, DefaultPermissionLevel.OP,
                "Allowed to spawn trainer with trainer editor");
    }

    byte                  message;
    public CompoundNBT data = new CompoundNBT();

    public static void sendEditOpenPacket(Entity target, ServerPlayerEntity editor)
    {
        String node = target == editor || target == null ? editor.isSneaking() ? EDITSELF : SPAWNTRAINER
                : target instanceof PlayerEntity ? EDITOTHER
                        : CapabilityHasPokemobs.getHasPokemobs(target) != null ? EDITTRAINER : EDITMOB;
        boolean canEdit = !editor.getServer().isDedicatedServer() || PermissionAPI.hasPermission(editor, node);

        if (!canEdit)
        {
            editor.sendMessage(new StringTextComponent(TextFormatting.RED + "You are not allowed to do that."));
            return;
        }
        PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
        packet.data.putBoolean("O", true);
        packet.data.putInt("I", target == null ? -1 : target.getEntityId());

        if (target != null)
        {
            CompoundNBT tag = new CompoundNBT();
            IHasNPCAIStates ai = CapabilityNPCAIStates.getNPCAIStates(target);
            IGuardAICapability guard = target.getCapability(EventsHandler.GUARDAI_CAP, null);
            IHasPokemobs pokemobs = CapabilityHasPokemobs.getHasPokemobs(target);
            if (ai != null)
                tag.setTag("A", CapabilityNPCAIStates.storage.writeNBT(CapabilityNPCAIStates.AISTATES_CAP, ai, null));
            if (guard != null) tag.setTag("G", EventsHandler.storage.writeNBT(EventsHandler.GUARDAI_CAP, guard, null));
            if (pokemobs != null) tag.setTag("P",
                    CapabilityHasPokemobs.storage.writeNBT(CapabilityHasPokemobs.HASPOKEMOBS_CAP, pokemobs, null));
            packet.data.setTag("C", tag);
        }
        PokecubeMod.packetPipeline.sendTo(packet, editor);
    }

    public PacketTrainer()
    {
    }

    public PacketTrainer(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketTrainer message, final MessageContext ctx)
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

    void processMessage(MessageContext ctx, PacketTrainer message)
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
        if (message.message == MESSAGEUPDATETRAINER)
        {
            int id = message.data.getInt("I");
            Entity mob = player.getEntityWorld().getEntityByID(id);

            // O for Open Gui Packet.
            if (message.data.getBoolean("O"))
            {
                if (mob != null && message.data.hasKey("C"))
                {
                    CompoundNBT nbt = message.data.getCompound("C");
                    IHasNPCAIStates ai = CapabilityNPCAIStates.getNPCAIStates(mob);
                    IGuardAICapability guard = mob.getCapability(EventsHandler.GUARDAI_CAP, null);
                    IHasPokemobs pokemobs = CapabilityHasPokemobs.getHasPokemobs(mob);
                    if (nbt.hasKey("A"))
                    {
                        if (ai != null)
                        {
                            CapabilityNPCAIStates.storage.readNBT(CapabilityNPCAIStates.AISTATES_CAP, ai, null,
                                    nbt.getTag("A"));
                        }
                    }
                    if (nbt.hasKey("G"))
                    {
                        if (guard != null)
                        {
                            EventsHandler.storage.readNBT(EventsHandler.GUARDAI_CAP, guard, null, nbt.getTag("G"));
                        }
                    }
                    if (nbt.hasKey("P"))
                    {
                        if (pokemobs != null)
                        {
                            CapabilityHasPokemobs.storage.readNBT(CapabilityHasPokemobs.HASPOKEMOBS_CAP, pokemobs, null,
                                    nbt.getTag("P"));
                        }
                    }
                }
                player.openGui(PokecubeAdv.instance, PokecubeAdv.GUITRAINER_ID, player.getEntityWorld(),
                        mob != null ? mob.getEntityId() : -1, 0, 0);
                return;
            }
            if (mob == null) return;
            IHasPokemobs cap = CapabilityHasPokemobs.getHasPokemobs(mob);
            IGuardAICapability guard = mob.getCapability(EventsHandler.GUARDAI_CAP, null);

            // Reset defeat list.
            if (message.data.getBoolean("RDL"))
            {
                cap.resetDefeatList();
                return;
            }

            INBT tag = message.data.getTag("T");
            if (tag instanceof CompoundNBT && ((CompoundNBT) tag).hasKey("GU") && guard != null)
            {
                RouteEditHelper.applyServerPacket(tag, mob, guard);
                return;
            }
            if (tag instanceof CompoundNBT && ((CompoundNBT) tag).hasKey("TR") && mob instanceof IMerchant)
            {
                CompoundNBT nbt = ((CompoundNBT) tag);
                int index = nbt.getInt("I");
                CompoundNBT tag2 = new CompoundNBT();
                mob.writeToNBT(tag2);
                MerchantRecipeList list = new MerchantRecipeList(tag2.getCompound("Offers"));
                if (nbt.hasKey("R"))
                {
                    MerchantRecipe recipe = new MerchantRecipe(nbt.getCompound("R"));
                    if (index < list.size()) list.set(index, recipe);
                    else list.add(recipe);
                }
                else
                {
                    if (nbt.hasKey("N"))
                    {
                        int index1 = nbt.getInt("I");
                        int index2 = index1 + nbt.getInt("N");
                        MerchantRecipe temp = list.get(index1);
                        list.set(index1, list.get(index2));
                        list.set(index2, temp);
                    }
                    else if (index < list.size()) list.remove(index);
                }

                tag2.setTag("Offers", list.getRecipiesAsTags());
                mob.readFromNBT(tag2);
                PacketHandler.sendEntityUpdate(mob);
                return;
            }
            else if (cap != null)
            {
                IHasMessages messages = CapabilityNPCMessages.getMessages(mob);
                IHasRewards rewards = CapabilityHasRewards.getHasRewards(mob);
                IHasNPCAIStates ai = CapabilityNPCAIStates.getNPCAIStates(mob);
                boolean hasAI = ai != null;
                ITextComponent mess = null;
                if (message.data.hasKey("X"))
                {
                    cap.setGender(message.data.getByte("X"));
                    mess = new TranslationTextComponent("traineredit.set.gender." + message.data.getByte("X"));
                    player.sendStatusMessage(mess, true);
                }
                if (message.data.hasKey("K"))
                {
                    TypeTrainer type = TypeTrainer.getTrainer(message.data.getString("K"));
                    if (type != cap.getType())
                    {
                        TypeTrainer old = cap.getType();
                        String prefix = old.name + " ";
                        if (mob.getName().startsWith(prefix))
                        {
                            mob.setCustomNameTag(mob.getName().replaceFirst(old.name, type.name));
                        }
                        mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, type.held.copy());
                        mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, type.bag.copy());
                        mess = new TranslationTextComponent("traineredit.set.type", type.name);
                        cap.setType(type);
                    }
                }
                if (message.data.hasKey("N"))
                {
                    mob.setCustomNameTag(message.data.getString("N"));
                    if (mob instanceof EntityTrainer)
                    {
                        ((EntityTrainer) mob).name = mob.getCustomNameTag().replaceFirst(cap.getType() + " ", "");
                    }
                    mess = new TranslationTextComponent("traineredit.set.name", message.data.getString("N"));
                }
                if (mob instanceof EntityTrainer)
                {
                    EntityTrainer trainer = (EntityTrainer) mob;
                    if (message.data.hasKey("U"))
                    {
                        trainer.urlSkin = message.data.getString("U");
                    }
                    if (message.data.hasKey("P"))
                    {
                        trainer.playerName = message.data.getString("P");
                    }
                }
                boolean stationaryBefore = hasAI ? ai.getAIState(IHasNPCAIStates.STATIONARY) : false;
                if (tag != null && !tag.hasNoTags())
                {
                    byte type = message.data.getByte("V");
                    if (type == 0)
                        CapabilityHasPokemobs.storage.readNBT(CapabilityHasPokemobs.HASPOKEMOBS_CAP, cap, null, tag);
                    else if (type == 1 && rewards != null)
                        CapabilityHasRewards.storage.readNBT(CapabilityHasRewards.REWARDS_CAP, rewards, null, tag);
                    else if (type == 2 && messages != null)
                        CapabilityNPCMessages.storage.readNBT(CapabilityNPCMessages.MESSAGES_CAP, messages, null, tag);
                    else if (type == 3 && ai != null)
                        CapabilityNPCAIStates.storage.readNBT(CapabilityNPCAIStates.AISTATES_CAP, ai, null, tag);
                    else if (type == 4 && guard != null)
                        EventsHandler.storage.readNBT(EventsHandler.GUARDAI_CAP, guard, null, tag);
                }
                if (mess != null) player.sendStatusMessage(mess, true);
                boolean stationaryNow = hasAI ? ai.getAIState(IHasNPCAIStates.STATIONARY) : false;
                if (stationaryNow != stationaryBefore)
                {
                    if (guard != null)
                    {
                        guard.getPrimaryTask().setPos(mob.getPosition());
                        guard.getPrimaryTask()
                                .setActiveTime(stationaryBefore ? new TimePeriod(0, 0) : TimePeriod.fullDay);
                    }
                }
                PacketHandler.sendEntityUpdate(mob);
            }
            return;
        }
        if (message.message == MESSAGEUPDATEMOB)
        {
            INBT tag = message.data.getTag("T");
            int id = message.data.getInt("I");
            Entity mob = player.getEntityWorld().getEntityByID(id);
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (pokemob != null)
            {
                IPokemob newPokemob = PokecubeManager.itemToPokemob(new ItemStack((CompoundNBT) tag),
                        mob.getEntityWorld());
                if (message.data.getBoolean("D"))
                {
                    mob.setDead();
                }
                else mob.readFromNBT(newPokemob.getEntity().writeToNBT(new CompoundNBT()));
                pokemob.readPokemobData(newPokemob.writePokemobData());
                pokemob.onGenesChanged();
                PacketHandler.sendEntityUpdate(mob);
            }
        }
        if (message.message == MESSAGESPAWNTRAINER)
        {
            int id = message.data.getInt("I");
            TypeTrainer type = TypeTrainer.getTrainer(message.data.getString("T"));
            if (type != null)
            {
                final int TRAINER = 0;
                final int LEADER = 1;
                final int TRADER = 2;
                EntityTrainer trainer = null;
                switch (id)
                {
                case TRAINER:
                    trainer = new EntityTrainer(player.getEntityWorld());
                    break;
                case LEADER:
                    trainer = new EntityLeader(player.getEntityWorld());
                    break;
                case TRADER:
                    trainer = new EntityPokemartSeller(player.getEntityWorld());
                    break;
                }
                if (trainer != null)
                {
                    trainer.initTrainer(type, 1);
                    Vector3 look = Vector3.getNewVector().set(player.getLookVec());
                    Vector3 pos = Vector3.getNewVector().set(player).addTo(look.x, 1, look.z);
                    pos.moveEntity(trainer);
                    player.getEntityWorld().spawnEntity(trainer);
                    sendEditOpenPacket(trainer, (ServerPlayerEntity) player);
                }
            }
            return;
        }
        if (message.message == MESSAGENOTIFYDEFEAT)
        {
            int id = message.data.getInt("I");
            LivingEntity mob = (LivingEntity) player.getEntityWorld().getEntityByID(id);
            if (mob instanceof EntityTrainer) ((EntityTrainer) mob).visibleTime = message.data.getLong("L");
            return;
        }
        if (message.message == MESSAGEKILLTRAINER)
        {
            int id = message.data.getInt("I");
            Entity trainer = player.getEntityWorld().getEntityByID(id);
            trainer.setDead();
        }
    }

}
