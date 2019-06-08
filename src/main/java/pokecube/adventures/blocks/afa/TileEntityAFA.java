package pokecube.adventures.blocks.afa;

import java.util.List;
import java.util.Random;

import org.nfunk.jep.JEP;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.commands.Config;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.lib.CompatWrapper;

public class TileEntityAFA extends TileEntityOwnable implements IInventory, ITickable
{
    public static JEP parser;
    public static JEP parserS;
    public IPokemob   pokemob        = null;
    boolean           shiny          = false;
    List<ItemStack>   inventory      = NonNullList.<ItemStack> withSize(1, ItemStack.EMPTY);
    public int[]      shift          = { 0, 0, 0 };
    public int        scale          = 1000;
    public String     animation      = "idle";
    public Ability    ability        = null;
    public int        energy         = 0;
    public int        distance       = 4;
    public int        transparency   = 128;
    public boolean    rotates        = true;
    public float      angle          = 0;
    public boolean    noEnergy       = false;
    public boolean    frozen         = true;
    public float      animationTime  = 0;

    protected boolean addedToNetwork = false;

    public static void initParser(String function, String functionS)
    {
        parser = new JEP();
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex(); // among other things adds i to the symbol
                             // table
        parser.addVariable("d", 0);
        parser.addVariable("l", 0);
        parser.parseExpression(function);

        parserS = new JEP();
        parserS.initFunTab(); // clear the contents of the function table
        parserS.addStandardFunctions();
        parserS.initSymTab(); // clear the contents of the symbol table
        parserS.addStandardConstants();
        parserS.addComplex(); // among other things adds i to the symbol
                              // table
        parserS.addVariable("d", 0);
        parserS.parseExpression(functionS);
    }

    public TileEntityAFA()
    {
        super();
    }

    @Override
    public void clear()
    {
        inventory.set(0, ItemStack.EMPTY);
    }

    @Override
    public void closeInventory(PlayerEntity player)
    {
    }

    @Override
    public ItemStack decrStackSize(int slot, int count)
    {
        if (CompatWrapper.isValid(inventory.get(slot)))
        {
            ItemStack itemStack;
            itemStack = inventory.get(slot).splitStack(count);
            if (!CompatWrapper.isValid(inventory.get(slot)))
            {
                inventory.set(slot, ItemStack.EMPTY);
            }
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot)
    {
        if (CompatWrapper.isValid(inventory.get(slot)))
        {
            ItemStack stack = inventory.get(slot);
            inventory.set(slot, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new StringTextComponent("Ability Field Amplifier");
    }

    @Override
    public int getField(int id)
    {
        if (id == 0) return energy;
        if (id == 1) return distance;
        if (id == 2) return noEnergy ? 1 : 0;
        if (id == 3) return scale;
        if (id == 4) return shift[0];
        if (id == 5) return shift[1];
        if (id == 6) return shift[2];
        return 0;
    }

    @Override
    public int getFieldCount()
    {
        return 8;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    public int getMaxEnergyStored(Direction facing)
    {
        return Config.instance.afaMaxEnergy;
    }

    @Override
    public String getName()
    {
        return "AFA";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        return bb;
    }

    @Override
    public int getSizeInventory()
    {
        return inventory.size();
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventory.get(index);
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

    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT nbt = new CompoundNBT();
        return writeToNBT(nbt);
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        if (ability != null) ability.destroy();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return PokecubeManager.isFilled(stack)
                || ItemStack.areItemStackTagsEqual(PokecubeItems.getStack("shiny_charm"), stack);
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return true;
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

    @Override
    public void openInventory(PlayerEntity player)
    {
    }

    @Override
    public void readFromNBT(CompoundNBT nbt)
    {
        super.readFromNBT(nbt);
        INBT temp = nbt.getTag("Inventory");
        if (temp instanceof ListNBT)
        {
            ListNBT tagList = (ListNBT) temp;
            for (int i = 0; i < tagList.size(); i++)
            {
                CompoundNBT tag = tagList.getCompound(i);
                byte slot = tag.getByte("Slot");

                if (slot >= 0 && slot < inventory.size())
                {
                    inventory.set(slot, new ItemStack(tag));
                }
            }
        }
        shift = nbt.getIntArray("shift");
        if (nbt.hasKey("scale")) scale = nbt.getInteger("scale");
        distance = nbt.getInteger("distance");
        noEnergy = nbt.getBoolean("noEnergy");
        angle = nbt.getFloat("angle");
        rotates = nbt.getBoolean("rotates");
        transparency = nbt.getInteger("transparency");
        energy = nbt.getInteger("energy");
        frozen = nbt.getBoolean("frozen");
        animationTime = nbt.getFloat("animTime");
        animation = nbt.getString("animation");
        shiny = Tools.isStack(inventory.get(0), "shiny_charm");
    }

    public int receiveEnergy(Direction facing, int maxReceive, boolean simulate)
    {
        int receive = Math.min(maxReceive, getMaxEnergyStored(facing) - energy);
        if (!simulate && receive > 0)
        {
            energy += receive;
        }
        return receive;
    }

    public void refreshAbility()
    {
        if (pokemob != null)
        {
            pokemob.getEntity().setDead();
            pokemob = null;
            ability = null;
        }
        if (ability != null) ability.destroy();
        shiny = Tools.isStack(inventory.get(0), "shiny_charm");
        if (!CompatWrapper.isValid(inventory.get(0))) return;
        if (ability != null)
        {
            ability.destroy();
            ability = null;
        }
        pokemob = PokecubeManager.itemToPokemob(inventory.get(0), getWorld());
        if (pokemob != null && pokemob.getAbility() != null)
        {
            ability = pokemob.getAbility();
            ability.destroy();
            pokemob.getEntity().setPosition(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
            ability.init(pokemob, distance);
        }
    }

    @Override
    public void setField(int id, int value)
    {
        if (id == 0) energy = value;
        if (id == 1) distance = value;
        if (id == 2) noEnergy = value != 0;
        if (id == 3) scale = value;
        if (id == 4) shift[0] = value;
        if (id == 5) shift[1] = value;
        if (id == 6) shift[2] = value;
        if (id == 7)
        {
            shift[0] = shift[1] = shift[2] = 0;
            scale = 1000;
        }
        distance = Math.max(0, distance);
        refreshAbility();
        if (!world.isRemote)
        {
            PacketHandler.sendTileUpdate(this);
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (CompatWrapper.isValid(stack)) inventory.set(index, ItemStack.EMPTY);
        inventory.set(index, stack);
    }

    @SubscribeEvent
    public void spawnEvent(SpawnEvent.Post evt)
    {
        if (shiny)
        {
            if (evt.location.distanceTo(Vector3.getNewVector().set(this)) <= distance)
            {
                Random rand = new Random();
                int rate = Math.max(PokecubeAdv.conf.afaShinyRate, 1);
                if (rand.nextInt(rate) == 0)
                {
                    if (!noEnergy && !world.isRemote)
                    {
                        parserS.setVarValue("d", distance);
                        double value = parserS.getValue();
                        int needed = (int) Math.ceil(value);
                        if (this.energy < needed)
                        {
                            energy = 0;
                            world.playSound(getPos().getX(), getPos().getY(), getPos().getZ(),
                                    SoundEvents.BLOCK_NOTE_BASEDRUM, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                            return;
                        }
                        energy -= needed;
                    }
                    evt.pokemob.setShiny(true);
                    world.playSound(evt.entity.posX, evt.entity.posY, evt.entity.posZ,
                            SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    world.playSound(getPos().getX(), getPos().getY(), getPos().getZ(),
                            SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                }
            }
        }
    }

    @Override
    public void update()
    {
        if (CompatWrapper.isValid(inventory.get(0)) && pokemob == null)
        {
            refreshAbility();
        }
        else if (!CompatWrapper.isValid(inventory.get(0)))
        {
            refreshAbility();
        }

        boolean shouldUseEnergy = pokemob != null && ability != null;
        int levelFactor = 0;
        if (pokemob != null && ability != null)
        {
            shiny = false;
        }

        if (shouldUseEnergy)
        {
            if (!noEnergy && !world.isRemote)
            {
                double value;
                if (shiny)
                {
                    parserS.setVarValue("d", distance);
                    value = parserS.getValue();
                }
                else
                {
                    parser.setVarValue("l", levelFactor);
                    parser.setVarValue("d", distance);
                    value = parser.getValue();
                }
                int needed = (int) Math.ceil(value);
                if (energy < needed)
                {
                    energy = 0;
                    return;
                }
                else energy -= needed;
            }
        }

        if (pokemob != null && ability != null)
        {
            shiny = false;
            // Tick increase incase ability tracks this for update.
            // Renderer can also then render it animated.
            pokemob.getEntity().ticksExisted++;
            levelFactor = pokemob.getLevel();
            // Do not call ability update on client.
            if (!world.isRemote) ability.onUpdate(pokemob);
        }
        shouldUseEnergy = shouldUseEnergy || shiny;
    }

    @Override
    public void validate()
    {
        super.validate();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT nbt)
    {
        super.writeToNBT(nbt);
        ListNBT itemList = new ListNBT();
        for (int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack;
            if (CompatWrapper.isValid(stack = inventory.get(i)))
            {
                CompoundNBT tag = new CompoundNBT();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        nbt.putIntArray("shift", shift);
        nbt.setInteger("scale", scale);
        nbt.setTag("Inventory", itemList);
        nbt.setInteger("distance", distance);
        nbt.putBoolean("noEnergy", noEnergy);
        nbt.putFloat("angle", angle);
        nbt.putBoolean("rotates", rotates);
        nbt.setInteger("transparency", transparency);
        nbt.setInteger("energy", energy);
        nbt.putBoolean("frozen", frozen);
        nbt.putFloat("animTime", animationTime);
        nbt.putString("animation", animation);
        return nbt;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }
}
