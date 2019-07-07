package pokecube.adventures;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.adventures.blocks.afa.AfaBlock;
import pokecube.adventures.client.ClientProxy;
import pokecube.adventures.entity.trainer.EntityTrainer;
import pokecube.core.PokecubeItems;

@Mod(value = PokecubeAdv.ID)
public class PokecubeAdv
{
    // You can use EventBusSubscriber to automatically subscribe events on
    // the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event)
        {
            if (!ModLoadingContext.get().getActiveContainer().getModId().equals(PokecubeAdv.ID)) return;
            // register blocks
            event.getRegistry().register(PokecubeAdv.AFA);
            event.getRegistry().register(PokecubeAdv.COMMANDER);
            event.getRegistry().register(PokecubeAdv.DAYCARE);
            event.getRegistry().register(PokecubeAdv.CLONER);
            event.getRegistry().register(PokecubeAdv.EXTRACTOR);
            event.getRegistry().register(PokecubeAdv.SPLICER);
            event.getRegistry().register(PokecubeAdv.SIPHON);
            event.getRegistry().register(PokecubeAdv.WARPPAD);
        }

        @SubscribeEvent
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
        {
            if (!ModLoadingContext.get().getActiveContainer().getModId().equals(PokecubeAdv.ID)) return;
            // register a new mob here
            event.getRegistry().register(EntityTrainer.TYPE.setRegistryName(PokecubeAdv.ID, "trainer"));
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            if (!ModLoadingContext.get().getActiveContainer().getModId().equals(PokecubeAdv.ID)) return;
            // register items

            // Register the item blocks.
            event.getRegistry().register(new BlockItem(PokecubeAdv.AFA, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.AFA.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.COMMANDER, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.COMMANDER.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.DAYCARE, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.DAYCARE.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.CLONER, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.CLONER.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.EXTRACTOR, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.EXTRACTOR.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.SPLICER, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.SPLICER.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.SIPHON, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.SIPHON.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.WARPPAD, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.WARPPAD.getRegistryName()));

            // Register the badges
            for (final Item item : PokecubeAdv.BADGES)
                event.getRegistry().register(item);
        }

        @SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
        {
            if (!ModLoadingContext.get().getActiveContainer().getModId().equals(PokecubeAdv.ID)) return;
            // register tile entities
        }
    }

    public static final String ID = "pokecube_adventures";

    public static final Block AFA;
    public static final Block COMMANDER;
    public static final Block DAYCARE;
    public static final Block CLONER;
    public static final Block EXTRACTOR;
    public static final Block SPLICER;
    public static final Block SIPHON;
    public static final Block WARPPAD;

    public static final List<Item> BADGES = Lists.newArrayList();

    static
    {
        AFA = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "afa");
        COMMANDER = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "commander");
        DAYCARE = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "daycare");
        CLONER = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "cloner");
        EXTRACTOR = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "extractor");
        SPLICER = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "splicer");
        SIPHON = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "siphon");
        WARPPAD = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "warppad");
    }

    public static final String TRAINERTEXTUREPATH = PokecubeAdv.ID + ":textures/trainer/";

    public final static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(),
            () -> () -> new CommonProxy());

    public static final Config config = Config.instance;

    public PokecubeAdv()
    {

        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeAdv.proxy::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeAdv.proxy::setupClient);
        // Register the loaded method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeAdv.proxy::loaded);

        MinecraftForge.EVENT_BUS.register(this);

        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(PokecubeAdv.config, PokecubeAdv.ID, PokecubeAdv.ID);
    }
}
