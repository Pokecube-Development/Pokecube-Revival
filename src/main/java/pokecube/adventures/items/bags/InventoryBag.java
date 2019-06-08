package pokecube.adventures.items.bags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.api.distmarker.Dist;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.CompatWrapper;

public class InventoryBag implements IInventory
{
    private static final String               FILEID    = "BagInventory";
    public static HashMap<UUID, InventoryBag> map       = new HashMap<UUID, InventoryBag>();
    public static UUID                        defaultID = new UUID(12345678910l, 12345678910l);
    public static UUID                        blankID   = new UUID(0, 0);
    public static int                         PAGECOUNT = 32;
    // blank bag for client use.
    public static InventoryBag                blank;

    public static void loadBag(UUID uuid)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Dist.CLIENT) return;
        try
        {
            File file = PlayerDataHandler.getFileForUUID(uuid.toString(), FILEID);
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                readBagFromNBT(CompoundNBT.getCompound("Data"));
            }
        }
        catch (FileNotFoundException e)
        {
        }
        catch (Exception e)
        {
        }
    }

    public static void readBagFromNBT(CompoundNBT nbt)
    {
        // Read PC Data from NBT
        INBT temp = nbt.getTag("PC");
        if (temp instanceof ListNBT)
        {
            ListNBT tagListPC = (ListNBT) temp;
            InventoryBag.loadFromNBT(tagListPC);
        }
    }

    public static void saveBag(String uuid)
    {

        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) return;
        try
        {
            File file = PlayerDataHandler.getFileForUUID(uuid, FILEID);
            if (file != null)
            {
                CompoundNBT CompoundNBT = new CompoundNBT();
                writeBagToNBT(CompoundNBT, uuid);
                CompoundNBT CompoundNBT1 = new CompoundNBT();
                CompoundNBT1.setTag("Data", CompoundNBT);
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(CompoundNBT1, fileoutputstream);
                fileoutputstream.close();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void writeBagToNBT(CompoundNBT nbt, String uuid)
    {
        ListNBT tagsPC = InventoryBag.saveToNBT(uuid);
        nbt.setTag("PC", tagsPC);
    }

    public static void clearInventory()
    {
        map.clear();
    }

    public static InventoryBag getBag(Entity player)
    {// TODO Sync box names to blank
        if (player.getEntityWorld().isRemote) return blank == null ? blank = new InventoryBag(blankID) : blank;
        return getBag(player.getUniqueID());
    }

    public static InventoryBag getBag(UUID uuid)
    {
        if (uuid != null)
        {
            if (map.containsKey(uuid))
            {
                return map.get(uuid);
            }
            else loadBag(uuid);
            if (map.containsKey(uuid)) { return map.get(uuid); }
            return new InventoryBag(uuid);
        }
        return getBag(defaultID);
    }

    public static void loadFromNBT(ListNBT nbt)
    {
        loadFromNBT(nbt, true);
    }

    public static void loadFromNBT(ListNBT nbt, boolean replace)
    {
        int i;
        tags:
        for (i = 0; i < nbt.size(); i++)
        {
            CompoundNBT items = nbt.getCompound(i);
            CompoundNBT boxes = items.getCompound("boxes");
            UUID uuid;
            try
            {
                uuid = UUID.fromString(boxes.getString("UUID"));
            }
            catch (Exception e)
            {
                continue;
            }
            if (uuid.equals(blankID)) continue;

            InventoryBag load = null;
            for (int k = 0; k < PAGECOUNT; k++)
            {
                if (k == 0)
                {
                    load = replace ? new InventoryBag(uuid) : getBag(uuid);

                    if (load == null) continue tags;
                    load.setPage(boxes.getInteger("page"));
                }
                if (boxes.getString("name" + k) != null)
                {
                    load.boxes[k] = boxes.getString("name" + k);
                }
            }
            if (load.getPage() >= PAGECOUNT) load.setPage(0);
            load.contents.clear();
            for (int k = 0; k < load.getSizeInventory(); k++)
            {
                if (!items.hasKey("item" + k)) continue;
                CompoundNBT CompoundNBT = items.getCompound("item" + k);
                int j = CompoundNBT.getShort("Slot");
                if (j >= 0 && j < load.getSizeInventory())
                {
                    if (load.contents.containsKey(j)) continue;
                    ItemStack itemstack = new ItemStack(CompoundNBT);
                    load.setInventorySlotContents(j, itemstack);
                }
            }
            map.put(uuid, load);
        }
    }

    public static ListNBT saveToNBT(Entity owner)
    {
        return saveToNBT(owner.getCachedUniqueIdString());
    }

    public static ListNBT saveToNBT(String uuid)
    {
        ListNBT nbttag = new ListNBT();
        UUID player = UUID.fromString(uuid);
        if (map.get(player) == null || blankID.equals(player)) { return nbttag; }
        CompoundNBT items = new CompoundNBT();
        CompoundNBT boxes = new CompoundNBT();
        boxes.putString("UUID", player.toString());
        boxes.setInteger("page", map.get(player).page);
        for (int i = 0; i < PAGECOUNT; i++)
        {
            boxes.putString("name" + i, map.get(player).boxes[i]);
        }
        items.setInteger("page", map.get(player).getPage());
        for (int i = 0; i < map.get(player).getSizeInventory(); i++)
        {
            ItemStack itemstack = map.get(player).getStackInSlot(i);
            CompoundNBT CompoundNBT = new CompoundNBT();

            if (!itemstack.isEmpty())
            {
                CompoundNBT.setShort("Slot", (short) i);
                itemstack.writeToNBT(CompoundNBT);
                items.setTag("item" + i, CompoundNBT);
            }
        }
        items.setTag("boxes", boxes);
        nbttag.appendTag(items);
        return nbttag;
    }

    private int                              page     = 0;
    public boolean[]                         opened   = new boolean[PAGECOUNT];
    public String[]                          boxes    = new String[PAGECOUNT];
    private Int2ObjectOpenHashMap<ItemStack> contents = new Int2ObjectOpenHashMap<>();
    public final UUID                        owner;
    boolean                                  dirty    = false;

    public InventoryBag(UUID uuid)
    {
        if (uuid != null) map.put(uuid, this);
        opened = new boolean[PAGECOUNT];
        boxes = new String[PAGECOUNT];
        owner = uuid;
        for (int i = 0; i < PAGECOUNT; i++)
        {
            boxes[i] = "Box " + String.valueOf(i + 1);
        }
    }

    public void addItem(ItemStack stack)
    {
        for (int i = page * 54; i < getSizeInventory(); i++)
        {
            if (this.getStackInSlot(i).isEmpty())
            {
                this.setInventorySlotContents(i, stack);
                return;
            }
        }
        for (int i = 0; i < page * 54; i++)
        {
            if (this.getStackInSlot(i).isEmpty())
            {
                this.setInventorySlotContents(i, stack);
                return;
            }
        }
    }

    @Override
    public void clear()
    {
        this.contents.clear();
    }

    @Override
    public void closeInventory(PlayerEntity player)
    {
        saveBag(player.getCachedUniqueIdString());
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        if (CompatWrapper.isValid(contents.get(i)))
        {
            ItemStack itemstack = contents.get(i).splitStack(j);
            if (!CompatWrapper.isValid(contents.get(i)))
            {
                contents.remove(i);
            }
            return itemstack;
        }
        return ItemStack.EMPTY;
    }

    public HashSet<ItemStack> getContents()
    {
        HashSet<ItemStack> ret = new HashSet<ItemStack>();
        for (int i : contents.keySet())
        {
            if (CompatWrapper.isValid(contents.get(i))) ret.add(contents.get(i));
        }
        return ret;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return null;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public String getName()
    {
        return null;
    }

    public int getPage()
    {
        return page;
    }

    @Override
    public int getSizeInventory()
    {
        return PAGECOUNT * 54;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        ItemStack stack = contents.get(i);
        if (stack == null) stack = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    /** Returns true if automation is allowed to insert the given stack
     * (ignoring stack size) into the given slot. */
    @Override
    public boolean isItemValidForSlot(int par1, ItemStack stack)
    {
        return ContainerBag.isItemValid(stack);
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity PlayerEntity)
    {
        return true;
    }

    @Override
    public void markDirty()
    {
        dirty = true;
    }

    @Override
    public void openInventory(PlayerEntity player)
    {
    }

    @Override
    public ItemStack removeStackFromSlot(int i)
    {
        ItemStack stack = contents.remove(i);
        if (stack == null) stack = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setField(int id, int value)
    {

    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        if (CompatWrapper.isValid(itemstack)) contents.put(i, itemstack);
        else contents.remove(i);
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    public CompoundNBT serializeBox(int box)
    {
        CompoundNBT items = new CompoundNBT();
        items.setInteger("box", box);
        int start = box * 54;
        for (int i = start; i < start + 54; i++)
        {
            ItemStack itemstack = map.get(owner).getStackInSlot(i);
            CompoundNBT CompoundNBT = new CompoundNBT();
            if (!itemstack.isEmpty())
            {
                CompoundNBT.setShort("Slot", (short) i);
                itemstack.writeToNBT(CompoundNBT);
                items.setTag("item" + i, CompoundNBT);
            }
        }
        return items;
    }

    public void deserializeBox(CompoundNBT nbt)
    {
        int start = nbt.getInteger("box") * 54;
        for (int i = start; i < start + 54; i++)
        {
            if (!nbt.hasKey("item" + i)) continue;
            CompoundNBT CompoundNBT = nbt.getCompound("item" + i);
            int j = CompoundNBT.getShort("Slot");
            if (j >= start && j < start + 54)
            {
                ItemStack itemstack = new ItemStack(CompoundNBT);
                this.setInventorySlotContents(j, itemstack);
            }
        }
    }

}
