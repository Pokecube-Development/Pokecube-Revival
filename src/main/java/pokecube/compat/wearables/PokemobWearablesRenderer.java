package pokecube.compat.wearables;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.modelloader.client.render.DefaultIModelRenderer;
import pokecube.modelloader.client.render.PokemobAnimationChanger;
import pokecube.modelloader.client.render.PokemobAnimationChanger.WornOffsets;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.model.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.x3d.X3dObject;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class PokemobWearablesRenderer
{

    @SideOnly(Side.CLIENT)
    private static class ArmourRenderWrapper extends X3dObject
    {
        public EntityLivingBase wearer;
        public ModelBiped       model;
        public ResourceLocation tex;

        // TODO make this take offsets from XML stuff
        public ArmourRenderWrapper(String name, Vector3 scale, Vector3 offset, Vector3 angles)
        {
            super(name);
            this.preTrans.set(offset);
            this.scale.x = (float) scale.x;
            this.scale.y = (float) scale.y;
            this.scale.z = (float) scale.z;
            float x = (float) angles.x;
            float y = (float) angles.y;
            float z = (float) angles.z;
            Vector4 angle = null;
            if (z != 0)
            {
                angle = new Vector4(0, 0, 1, z);
            }
            if (x != 0)
            {
                if (angle != null)
                {
                    angle = angle.addAngles(new Vector4(1, 0, 0, x));
                }
                else
                {
                    angle = new Vector4(1, 0, 0, x);
                }
            }
            if (y != 0)
            {
                if (angle != null)
                {
                    angle = angle.addAngles(new Vector4(0, 1, 0, y));
                }
                else
                {
                    angle = new Vector4(0, 1, 0, y);
                }
            }
            if (angle != null) this.preRot.set(angle.x, angle.y, angle.z, angle.w);
        }

        @Override
        public void setPreRotations(Vector4 angles)
        {
            // We set these ourselves, so don't want to interfere.
        }

        @Override
        public void setPreTranslations(Vector3 point)
        {
            // We set these ourselves, so don't want to interfere.
        }

        @Override
        public void addForRender()
        {
            Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
            model.render(wearer, 0, 0, 0, 0, 0, 1);
        }

        @Override
        public void resetToInit()
        {
            postRot.set(0, 1, 0, 0);
            postRot1.set(0, 1, 0, 0);

            // We modify these seperately outselves.
            // preRot.set(0, 1, 0, 0);
            // preTrans.clear();

            postTrans.clear();
        }

        @Override
        public String getType()
        {
            return "_internal_";
        }
    }

    @SideOnly(Side.CLIENT)
    private static class WearableRenderWrapper extends X3dObject
    {
        public IWearable        wrapped;
        public EnumWearable     slot;
        public EntityLivingBase wearer;
        public ItemStack        stack;

        // TODO make this take offsets from XML stuff
        public WearableRenderWrapper(String name, Vector3 scale, Vector3 offset, Vector3 angles)
        {
            super(name);
            this.preTrans.set(offset);
            this.scale.x = (float) scale.x;
            this.scale.y = (float) scale.y;
            this.scale.z = (float) scale.z;
            float x = (float) angles.x;
            float y = (float) angles.y;
            float z = (float) angles.z;
            Vector4 angle = null;
            if (z != 0)
            {
                angle = new Vector4(0, 0, 1, z);
            }
            if (x != 0)
            {
                if (angle != null)
                {
                    angle = angle.addAngles(new Vector4(1, 0, 0, x));
                }
                else
                {
                    angle = new Vector4(1, 0, 0, x);
                }
            }
            if (y != 0)
            {
                if (angle != null)
                {
                    angle = angle.addAngles(new Vector4(0, 1, 0, y));
                }
                else
                {
                    angle = new Vector4(0, 1, 0, y);
                }
            }
            if (angle != null) this.preRot.set(angle.x, angle.y, angle.z, angle.w);
        }

        @Override
        public void setPreRotations(Vector4 angles)
        {
            // We set these ourselves, so don't want to interfere.
        }

        @Override
        public void setPreTranslations(Vector3 point)
        {
            // We set these ourselves, so don't want to interfere.
        }

        @Override
        public void addForRender()
        {
            wrapped.renderWearable(slot, wearer, stack, 0);
        }

        @Override
        public void resetToInit()
        {
            postRot.set(0, 1, 0, 0);
            postRot1.set(0, 1, 0, 0);

            // We modify these seperately outselves.
            // preRot.set(0, 1, 0, 0);
            // preTrans.clear();

            postTrans.clear();
        }

        @Override
        public String getType()
        {
            return "_internal_";
        }

    }

    static final String       helm_ident  = "__helm__";
    static final String       chest_ident = "__chest__";
    static final String       legs_ident  = "__legs__";
    static final String       boots_ident = "__boots__";

    static final List<String> addedNames  = Lists.newArrayList("__helm__", "__chest__", "__legs__", "__boots__");

    static
    {
        for (EnumWearable wearable : EnumWearable.values())
        {
            if (wearable.slots == 2)
            {
                addedNames.add("__" + wearable + "_right__");
                addedNames.add("__" + wearable + "_left__");
            }
            else
            {
                addedNames.add("__" + wearable + "__");
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Method(modid = "thut_wearables")
    @CompatClass(phase = Phase.PRE)
    public static void preInitWearables()
    {
        MinecraftForge.EVENT_BUS.register(PokemobWearablesRenderer.class);
    }

    @SubscribeEvent
    public static void postRender(RenderLivingEvent.Post<?> event)
    {
        if (event.getRenderer() instanceof DefaultIModelRenderer)
        {
            DefaultIModelRenderer<?> renderer = (DefaultIModelRenderer<?>) event.getRenderer();
            removeWearables(renderer, renderer.model.imodel);
        }
    }

    @SubscribeEvent
    public static void preRender(RenderLivingEvent.Pre<?> event)
    {
        if (event.getRenderer() instanceof DefaultIModelRenderer)
        {
            DefaultIModelRenderer<?> renderer = (DefaultIModelRenderer<?>) event.getRenderer();
            applyWearables(event.getEntity(), renderer, renderer.model.imodel);
        }
    }

    private static IWearable getWearable(ItemStack stack)
    {
        if (stack.getItem() instanceof IWearable) { return (IWearable) stack.getItem(); }
        return stack.getCapability(IActiveWearable.WEARABLE_CAP, null);
    }

    public static void removeWearables(IModelRenderer<?> renderer, IModel wrapper)
    {
        // TODO better way to determine whether we have these parts.
        List<IExtendedModelPart> added = Lists.newArrayList();

        for (String name : addedNames)
        {
            if (wrapper.getParts().containsKey(name))
            {
                added.add(wrapper.getParts().get(name));
            }
        }
        for (IExtendedModelPart part : added)
        {
            part.getParent().getSubParts().remove(part.getName());
            wrapper.getParts().remove(part.getName());
        }
    }

    public static WornOffsets getPartParent(EntityLivingBase wearer, IModelRenderer<?> renderer, IModel imodel,
            String identifier)
    {
        IAnimationChanger temp = renderer.getAnimationChanger();
        if (temp instanceof PokemobAnimationChanger) { return ((PokemobAnimationChanger) temp).wornOffsets
                .get(identifier); }
        return null;
    }

    public static void applyWearables(EntityLivingBase wearer, IModelRenderer<?> renderer, IModel imodel)
    {
        // No Render invisible.
        if (wearer.getActivePotionEffect(MobEffects.INVISIBILITY) != null) return;
        WornOffsets offsets = null;

        for (ItemStack stack : wearer.getArmorInventoryList())
        {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemArmor)
            {
                ItemArmor armour = (ItemArmor) stack.getItem();
                ArmourRenderWrapper wrapper;
                LayerBipedArmor temp = new LayerBipedArmor(null);
                ModelBiped model = new ModelBiped(.0F);
                model.setVisible(false);
                switch (armour.armorType)
                {
                case CHEST:
                    // TODO what to do for feet.
                    break;
                case FEET:
                    // TODO what to do for feet.
                    break;
                case HEAD:
                    model.bipedHead.showModel = true;
                    offsets = getPartParent(wearer, renderer, imodel, helm_ident);
                    if (offsets == null || !imodel.getParts().containsKey(offsets.parent)) break;

                    wrapper = new ArmourRenderWrapper(helm_ident, offsets.scale, offsets.offset, offsets.angles);
                    wrapper.wearer = wearer;
                    wrapper.model = model;
                    wrapper.model = net.minecraftforge.client.ForgeHooksClient.getArmorModel(wearer, stack,
                            EntityEquipmentSlot.HEAD, wrapper.model);
                    wrapper.tex = temp.getArmorResource(wearer, stack, EntityEquipmentSlot.HEAD, null);
                    IExtendedModelPart part = imodel.getParts().get(offsets.parent);
                    wrapper.setParent(part);
                    part.addChild(wrapper);
                    imodel.getParts().put(helm_ident, wrapper);
                    break;
                case LEGS:
                    // TODO what to do for feet.
                    break;
                default:
                    break;

                }
            }
        }

        PlayerWearables worn = ThutWearables.getWearables(wearer);
        for (EnumWearable wearable : EnumWearable.values())
        {
            int num = wearable.slots;
            for (int i = 0; i < num; i++)
            {
                String ident = "__" + wearable + "__";
                if (num > 1)
                {
                    ident = i == 0 ? "__" + wearable + "_right__" : "__" + wearable + "_left__";
                }
                IWearable w = getWearable(worn.getWearable(wearable, i));
                if (w == null) continue;
                offsets = getPartParent(wearer, renderer, imodel, ident);
                if (offsets != null && imodel.getParts().containsKey(offsets.parent))
                {
                    WearableRenderWrapper wrapper = new WearableRenderWrapper(ident, offsets.scale, offsets.offset,
                            offsets.angles);
                    wrapper.slot = wearable;
                    wrapper.wearer = wearer;
                    wrapper.stack = worn.getWearable(wearable);
                    wrapper.wrapped = w;
                    IExtendedModelPart part = imodel.getParts().get(offsets.parent);
                    wrapper.setParent(part);
                    part.addChild(wrapper);
                    imodel.getParts().put(ident, wrapper);
                }
            }
        }
    }
}
