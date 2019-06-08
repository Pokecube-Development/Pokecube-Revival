package pokecube.compat.wearables;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.adventures.CommonProxy;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.items.bags.ItemBag;
import pokecube.core.PokecubeCore;
import pokecube.core.events.pokemob.InteractEvent;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.ContainerWearables;

public class WearableCompat
{
    private static int GUID = 240;

    @Method(modid = "thut_wearables")
    @CompatClass(phase = Phase.PRE)
    public static void preInitWearables()
    {
        MinecraftForge.EVENT_BUS.register(new WearableCompat());
    }

    @Method(modid = "thut_wearables")
    @OnlyIn(Dist.CLIENT)
    @CompatClass(phase = Phase.PRE)
    public static void clientInitWearables()
    {
        IGuiHandler handler = new IGuiHandler()
        {

            @Override
            public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
            {
                return null;
            }

            @Override
            public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
            {
                Entity mob = PokecubeCore.core.getEntityProvider().getEntity(world, x, true);
                if (!(mob instanceof LivingEntity)) return null;
                LivingEntity base = (LivingEntity) mob;
                if (ThutWearables.getWearables(
                        base) != null) { return new thut.wearables.client.gui.GuiWearables(base, player); }
                return null;
            }
        };
        CommonProxy.custom_client_handlers.put(GUID, handler);
    }

    @Method(modid = "thut_wearables")
    @OnlyIn(Dist.DEDICATED_SERVER)
    @CompatClass(phase = Phase.PRE)
    public static void serverInitWearables()
    {
        IGuiHandler handler = new IGuiHandler()
        {

            @Override
            public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
            {
                Entity mob = PokecubeCore.core.getEntityProvider().getEntity(world, x, true);
                if (!(mob instanceof LivingEntity)) return null;
                LivingEntity base = (LivingEntity) mob;
                if (ThutWearables.getWearables(base) != null) { return new ContainerWearables(base, player); }
                return null;
            }

            @Override
            public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
            {
                return null;
            }
        };
        CommonProxy.custom_server_handlers.put(GUID, handler);
    }

    public WearableCompat()
    {
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getObject().getItem() instanceof ItemBag)
        {
            event.addCapability(new ResourceLocation("pokecube_adv:wearable"), new WearableBag());
        }
    }

    @SubscribeEvent
    public void interactWithPokemob(InteractEvent event)
    {
        if (event.pokemob.getOwner() == event.player)
        {
            PlayerEntity player = event.player;
            Hand hand = event.event.getHand();
            ItemStack held = player.getHeldItem(hand);
            if (held.getDisplayName().equalsIgnoreCase("wearables"))
            {
                player.openGui(PokecubeAdv.instance, GUID, player.getEntityWorld(),
                        event.pokemob.getEntity().getEntityId(), 0, 0);
                event.setResult(Result.DENY);
            }
        }
    }

    public static class WearableBag implements IActiveWearable, ICapabilityProvider
    {
        @Override
        public EnumWearable getSlot(ItemStack stack)
        {
            return EnumWearable.BACK;
        }

        // One model for each layer.
        @OnlyIn(Dist.CLIENT)
        X3dModel                 bag;

        // One Texture for each layer.
        private ResourceLocation BAG_1 = new ResourceLocation(PokecubeAdv.ID, "textures/worn/bag_1.png");
        private ResourceLocation BAG_2 = new ResourceLocation(PokecubeAdv.ID, "textures/worn/bag_2.png");

        @OnlyIn(Dist.CLIENT)
        @Override
        public void renderWearable(EnumWearable slot, LivingEntity wearer, ItemStack stack, float partialTicks)
        {
            if (bag == null)
            {
                bag = new X3dModel(new ResourceLocation(PokecubeAdv.ID, "models/worn/bag.x3d"));
            }
            int brightness = wearer.getBrightnessForRender();
            ResourceLocation pass1 = BAG_1;
            ResourceLocation pass2 = BAG_2;
            GL11.glPushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // First pass of render
            GL11.glPushMatrix();
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslated(0, -0.125, -0.6);
            GL11.glScaled(0.7, 0.7, 0.7);
            Minecraft.getInstance().renderEngine.bindTexture(pass1);
            int[] col = new int[] { 255, 255, 255, 255, brightness };
            for (IExtendedModelPart part1 : bag.getParts().values())
            {
                part1.setRGBAB(col);
            }
            bag.renderAll();
            GL11.glPopMatrix();

            // Second pass with colour.
            GL11.glPushMatrix();
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslated(0, -0.125, -0.6);
            GL11.glScaled(0.7, 0.7, 0.7);
            Minecraft.getInstance().renderEngine.bindTexture(pass2);
            EnumDyeColor ret = EnumDyeColor.YELLOW;
            if (stack.hasTag() && stack.getTag().hasKey("dyeColour"))
            {
                int damage = stack.getTag().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            Color colour = new Color(ret.getColorValue() + 0xFF000000);
            col[0] = colour.getRed();
            col[1] = colour.getGreen();
            col[2] = colour.getBlue();
            for (IExtendedModelPart part : bag.getParts().values())
            {
                part.setRGBAB(col);
            }
            bag.renderAll();
            GL11.glColor3f(1, 1, 1);
            GL11.glPopMatrix();

            GL11.glPopMatrix();
        }

        @Override
        public boolean hasCapability(Capability<?> capability, Direction facing)
        {
            return capability == WEARABLE_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, Direction facing)
        {
            return hasCapability(capability, facing) ? IActiveWearable.WEARABLE_CAP.cast(this) : null;
        }

        @Override
        public boolean dyeable(ItemStack stack)
        {
            return true;
        }
    }
}
