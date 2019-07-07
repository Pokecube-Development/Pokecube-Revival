package pokecube.adventures.client;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.adventures.CommonProxy;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.entity.trainer.EntityTrainer;
import pokecube.core.client.render.RenderNPC;

public class ClientProxy extends CommonProxy
{
    private static Map<TypeTrainer, ResourceLocation> males   = Maps.newHashMap();
    private static Map<TypeTrainer, ResourceLocation> females = Maps.newHashMap();

    @Override
    public ResourceLocation getTrainerSkin(final LivingEntity mob, final TypeTrainer type, final byte gender)
    {
        ResourceLocation texture = null;
        boolean male;
        if (male = gender == 1) texture = ClientProxy.males.get(type);
        else texture = ClientProxy.females.get(type);
        if (texture == null)
        {
            texture = type.getTexture(mob);

            if (male) ClientProxy.males.put(type, texture);
            else ClientProxy.females.put(type, texture);
        }
        return texture;
    }

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityTrainer.class, (manager) -> new RenderNPC<>(manager));
    }
}
