package pokecube.adventures.events;

import java.util.Random;
import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INpc;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.helper.AIStuffHolder;
import pokecube.adventures.ai.tasks.AIBattle;
import pokecube.adventures.ai.tasks.AIFindTarget;
import pokecube.adventures.ai.tasks.AIMate;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.commands.Config;
import pokecube.adventures.entity.helper.EntityTrainerBase;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.items.ItemTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.PCEvent;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.events.pokemob.InteractEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.moves.TerrainDamageSource;
import pokecube.core.utils.PokeType;
import thut.api.entity.ai.EntityAIBaseManager;
import thut.api.entity.ai.IAIMob;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.SyncHandler;
import thut.core.common.world.mobs.data.types.Data_ItemStack;
import thut.core.common.world.mobs.data.types.Data_String;

public class PAEventsHandler
{
    final ResourceLocation POKEMOBSCAP = new ResourceLocation(PokecubeAdv.ID, "pokemobs");
    final ResourceLocation AICAP       = new ResourceLocation(PokecubeAdv.ID, "ai");
    final ResourceLocation MESSAGECAP  = new ResourceLocation(PokecubeAdv.ID, "messages");
    final ResourceLocation REWARDSCAP  = new ResourceLocation(PokecubeAdv.ID, "rewards");
    final ResourceLocation AISTUFFCAP  = new ResourceLocation(PokecubeAdv.ID, "aiStuff");
    final ResourceLocation DATASCAP    = new ResourceLocation(PokecubeAdv.ID, "data");

    public static void randomizeTrainerTeam(Entity trainer, IHasPokemobs mobs)
    {
        Vector3 loc = Vector3.getNewVector().set(trainer);
        // Set level based on what wild pokemobs have.
        int level = SpawnHandler.getSpawnLevel(trainer.getEntityWorld(), loc, Pokedex.getInstance().getFirstEntry());
        if (trainer instanceof EntityLeader)
        {
            // Gym leaders are 10 lvls higher than others.
            level += 10;
            // Randomize badge for leader.
            if (((EntityLeader) trainer).randomBadge())
            {
                IHasRewards rewardsCap = ((EntityLeader) trainer).rewardsCap;
                PokeType type = PokeType.values()[new Random().nextInt(PokeType.values().length)];
                Item item = Item.getByNameOrId(PokecubeAdv.ID + ":badge_" + type);
                if (item != null)
                {
                    ItemStack badge = new ItemStack(item);
                    if (!rewardsCap.getRewards().isEmpty()) rewardsCap.getRewards().set(0, new Reward(badge));
                    else rewardsCap.getRewards().add(new Reward(badge));
                    ((EntityLeader) trainer).setHeldItem(Hand.OFF_HAND, rewardsCap.getRewards().get(0).stack);
                }
            }
        }
        // Randomize team.
        if (trainer instanceof EntityTrainer)
        {
            EntityTrainer t = (EntityTrainer) trainer;
            t.name = "";
            // Reset their trades, as this will randomize them when trades are
            // needed later.
            t.populateBuyingList(null);
            // Init for trainers randomizes their teams
            if (mobs.getType() != null) t.initTrainer(mobs.getType(), level);
        }
        else if (mobs.getType() != null)
        {
            mobs.setType(mobs.getType());
            byte genders = mobs.getType().genders;
            if (genders == 1) mobs.setGender((byte) 1);
            if (genders == 2) mobs.setGender((byte) 2);
            if (genders == 3) mobs.setGender((byte) (Math.random() < 0.5 ? 1 : 2));
            TypeTrainer.getRandomTeam(mobs, (LivingEntity) trainer, level, trainer.getEntityWorld());
        }
    }

    @SubscribeEvent
    /** This method will make sure that cloned selectors do not keep any
     * additional effects added to them.
     * 
     * @param event */
    public void BookCloneRecipeHandle(ItemCraftedEvent event)
    {
        // Not a selector, we do nothing.
        if (!RecipeSelector.isSelector(event.crafting) || !event.crafting.hasTag()) return;
        SelectorValue value = ClonerHelper.getSelectorValue(event.crafting);
        for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++)
        {
            ItemStack temp = event.craftMatrix.getStackInSlot(i);
            // If one of the ingredients is a selector, then we will reset the
            // generated selector.
            if (RecipeSelector.isSelector(temp))
            {
                SelectorValue value2 = ClonerHelper.getSelectorValue(temp);
                // Reset the selector if it is, infact, the same selector.
                // Otherwise it is a newly upgraded one.
                if (value2.dnaDestructChance == value.dnaDestructChance
                        && value2.selectorDestructChance == value.selectorDestructChance)
                {
                    event.crafting.getTag().remove(ClonerHelper.SELECTORTAG);
                }
                return;
            }
        }
    }

    @SubscribeEvent
    /** This prevents trainer's pokemobs going to PC
     * 
     * @param evt */
    public void TrainerPokemobPC(PCEvent evt)
    {
        if (evt.owner instanceof EntityTrainer)
        {
            evt.setCanceled(true);
        }
    }

    @SubscribeEvent(receiveCanceled = false)
    /** This sends pokemobs back to their NPC trainers when they are recalled.
     * 
     * @param evt */
    public void TrainerRecallEvent(pokecube.core.events.pokemob.RecallEvent evt)
    {
        IPokemob recalled = evt.recalled;
        LivingEntity owner = recalled.getPokemonOwner();
        if (owner == null) return;
        IHasPokemobs pokemobHolder = CapabilityHasPokemobs.getHasPokemobs(owner);
        if (pokemobHolder != null)
        {
            if (recalled == pokemobHolder.getOutMob())
            {
                pokemobHolder.setOutMob(null);
            }
            pokemobHolder.addPokemob(PokecubeManager.pokemobToItem(recalled));
        }
    }

    @SubscribeEvent
    /** This takes care of randomization for trainer teams when spawned in
     * structuress.
     * 
     * @param event */
    public void StructureSpawn(StructureEvent.SpawnEntity event)
    {
        IHasPokemobs mobs = CapabilityHasPokemobs.getHasPokemobs(event.getEntity());
        if (mobs == null) return;
        boolean randomize = event.getEntity().getEntityData().getBoolean("randomizeTeam");
        if (event.getEntity() instanceof EntityTrainer)
        {
            randomize = ((EntityTrainer) event.getEntity()).getShouldRandomize();
        }
        if (randomize)
        {
            randomizeTrainerTeam(event.getEntity(), mobs);
        }
    }

    @SubscribeEvent
    /** This assigns subbiomes for structures which spawn.
     * 
     * @param event */
    public void StructureBuild(StructureEvent.BuildStructure event)
    {
        String name = event.getStructure();
        int biome;
        if (event.getBiomeType() != null)
        {
            biome = BiomeType.getBiome(event.getBiomeType()).getType();
        }
        else
        {
            if (name == null
                    || !Config.biomeMap.containsKey(name = name.toLowerCase(java.util.Locale.ENGLISH))) { return; }
            biome = Config.biomeMap.get(name);
        }
        Vector3 pos = Vector3.getNewVector();
        StructureBoundingBox bounds = event.getBoundingBox();
        for (int i = bounds.minX; i <= bounds.maxX; i++)
        {
            for (int k = bounds.minZ; k <= bounds.maxZ; k++)
                if (event.getWorld().isChunkGeneratedAt(i >> 4, k >> 4))
                {
                    for (int j = bounds.minY; j <= bounds.maxY; j++)
                    {
                        {
                            pos.set(i, j, k);
                            TerrainManager.getInstance().getTerrian(event.getWorld(), pos).setBiome(pos, biome);
                        }
                    }
                }
        }
    }

    @SubscribeEvent
    /** This links the pokemob to the trainer when it is sent out.
     * 
     * @param evt */
    public void TrainerSendOutEvent(SendOut.Post evt)
    {
        IPokemob sent = evt.pokemob;
        LivingEntity owner = sent.getPokemonOwner();
        if (owner == null || owner instanceof PlayerEntity) return;
        IHasPokemobs pokemobHolder = CapabilityHasPokemobs.getHasPokemobs(owner);
        if (pokemobHolder != null)
        {
            if (pokemobHolder.getOutMob() != null && pokemobHolder.getOutMob() != evt.pokemob)
            {
                pokemobHolder.getOutMob().returnToPokecube();
                pokemobHolder.setOutMob(evt.pokemob);
            }
            else
            {
                pokemobHolder.setOutMob(evt.pokemob);
            }
            IHasNPCAIStates aiStates = CapabilityNPCAIStates.getNPCAIStates(owner);
            if (aiStates != null) aiStates.setAIState(IHasNPCAIStates.THROWING, false);
        }
    }

    @SubscribeEvent
    /** This manages invulnerability of npcs to pokemobs, as well as managing
     * the target allocation for trainers.
     * 
     * @param evt */
    public void livingHurtEvent(LivingHurtEvent evt)
    {
        IHasPokemobs pokemobHolder = CapabilityHasPokemobs.getHasPokemobs(evt.getMobEntity());
        IHasMessages messages = CapabilityNPCMessages.getMessages(evt.getMobEntity());

        if (evt.getMobEntity() instanceof INpc && !Config.instance.pokemobsHarmNPCs
                && (evt.getSource() instanceof PokemobDamageSource || evt.getSource() instanceof TerrainDamageSource))
        {
            evt.setAmount(0);
        }

        if (evt.getSource().getTrueSource() instanceof LivingEntity)
        {
            if (messages != null)
            {
                messages.sendMessage(MessageState.HURT, evt.getSource().getTrueSource(),
                        evt.getMobEntity().getDisplayName(), evt.getSource().getTrueSource().getDisplayName());
                messages.doAction(MessageState.HURT, (LivingEntity) evt.getSource().getTrueSource());
            }
            if (pokemobHolder != null && pokemobHolder.getTarget() == null)
            {
                pokemobHolder.setTarget((LivingEntity) evt.getSource().getTrueSource());
            }
        }
    }

    /** For custom item interactions with pokemobs.
     * 
     * @param event */
    @SubscribeEvent
    public void interactWithPokemob(InteractEvent event)
    {
        PlayerEntity player = event.player;
        Hand hand = event.event.getHand();
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() instanceof ItemTrainer)
        {
            PacketTrainer.sendEditOpenPacket(event.pokemob.getEntity(), (ServerPlayerEntity) player);
            event.setResult(Result.DENY);
        }
    }

    @SubscribeEvent
    /** Calls processInteract
     * 
     * @param evt */
    public void interactEvent(PlayerInteractEvent.EntityInteractSpecific evt)
    {
        if (evt.getWorld().isRemote) return;
        String ID = "LastSuccessInteractEvent";
        long time = evt.getTarget().getEntityData().getLong(ID);
        if (time == evt.getTarget().getEntityWorld().getGameTime()) return;
        processInteract(evt, evt.getTarget());
        evt.getTarget().getEntityData().putLong(ID, evt.getTarget().getEntityWorld().getGameTime());
    }

    @SubscribeEvent
    /** Calls processInteract
     * 
     * @param evt */
    public void interactEvent(PlayerInteractEvent.EntityInteract evt)
    {
        if (evt.getWorld().isRemote) return;
        String ID = "LastSuccessInteractEvent";
        long time = evt.getTarget().getEntityData().getLong(ID);
        if (time == evt.getTarget().getEntityWorld().getGameTime()) return;
        processInteract(evt, evt.getTarget());
        evt.getTarget().getEntityData().putLong(ID, evt.getTarget().getEntityWorld().getGameTime());
    }

    /** This deals with the interaction logic for trainers. It sends the
     * messages for MessageState.INTERACT, as well as applies the doAction. It
     * also handles opening the edit gui for the trainers when the player has
     * the trainer editor.
     * 
     * @param evt
     * @param target */
    public void processInteract(PlayerInteractEvent evt, Entity target)
    {
        IHasMessages messages = CapabilityNPCMessages.getMessages(target);
        IHasPokemobs pokemobs = CapabilityHasPokemobs.getHasPokemobs(target);
        if (!target.isSneaking() && pokemobs != null && evt.getItemStack().getItem() instanceof ItemTrainer)
        {
            evt.setCanceled(true);
            if (evt.getPlayerEntity() instanceof ServerPlayerEntity)
            {
                PacketTrainer.sendEditOpenPacket(target, (ServerPlayerEntity) evt.getPlayerEntity());
            }
            return;
        }
        if (messages != null)
        {
            messages.sendMessage(MessageState.INTERACT, evt.getPlayerEntity(), target.getDisplayName(),
                    evt.getPlayerEntity().getDisplayName());
            messages.doAction(MessageState.INTERACT, evt.getPlayerEntity());
        }
    }

    @SubscribeEvent
    /** Ensures the IHasPokemobs object has synced target with the MobEntity
     * object.
     * 
     * @param evt */
    public void livingSetTargetEvent(LivingSetAttackTargetEvent evt)
    {
        if (evt.getTarget() == null) return;
        IHasPokemobs pokemobHolder = CapabilityHasPokemobs.getHasPokemobs(evt.getTarget());
        if (pokemobHolder != null && pokemobHolder.getTarget() == null)
        {
            pokemobHolder.setTarget(evt.getMobEntity());
        }
    }

    @SubscribeEvent
    /** This manages making of trainers invisible if they have been defeated, if
     * this is enabled for the given trainer.
     * 
     * @param event */
    public void TrainerWatchEvent(StartTracking event)
    {
        if (event.getEntity().getEntityWorld().isRemote) return;
        IHasPokemobs mobs = CapabilityHasPokemobs.getHasPokemobs(event.getEntity());
        if (!(mobs instanceof DefaultPokemobs)) return;
        DefaultPokemobs pokemobs = (DefaultPokemobs) mobs;
        if (event.getPlayerEntity() instanceof ServerPlayerEntity)
        {
            EntityTrainer trainer = (EntityTrainer) event.getTarget();
            if (pokemobs.notifyDefeat)
            {
                PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGENOTIFYDEFEAT);
                packet.data.putInt("I", trainer.getEntityId());
                packet.data.putBoolean("V", pokemobs.hasDefeated(event.getPlayerEntity()));
                PokecubeMod.packetPipeline.sendTo(packet, (ServerPlayerEntity) event.getPlayerEntity());
            }
        }
    }

    @SubscribeEvent
    /** Attaches the various capabilities.
     * 
     * @param event */
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof MobEntity) || event.getObject().getEntityWorld() == null
                || TypeTrainer.mobTypeMapper.getType((LivingEntity) event.getObject(), false) == null)
            return;
        if (hasCap(event)) return;
        DefaultPokemobs mobs = new DefaultPokemobs();
        DefaultRewards rewards = new DefaultRewards();

        ItemStack stack = ItemStack.EMPTY;
        try
        {
            stack = fromString(PokecubeAdv.conf.defaultReward, event.getObject());
        }
        catch (CommandException e)
        {
            PokecubeMod.log(Level.WARNING, "Error with default trainer rewards " + PokecubeAdv.conf.defaultReward, e);
        }
        if (!stack.isEmpty()) rewards.getRewards().add(new Reward(stack));
        DefaultAIStates aiStates = new DefaultAIStates();
        DefaultMessager messages = new DefaultMessager();
        mobs.init((LivingEntity) event.getObject(), aiStates, messages, rewards);
        event.addCapability(POKEMOBSCAP, mobs);
        event.addCapability(AICAP, aiStates);
        event.addCapability(MESSAGECAP, messages);
        event.addCapability(REWARDSCAP, rewards);

        DataSync data = getData(event);
        if (data == null)
        {
            data = new DataSync_Impl();
            event.addCapability(DATASCAP, (DataSync_Impl) data);
        }
        mobs.datasync = data;
        mobs.holder.TYPE = data.register(new Data_String(), "");

        for (int i = 0; i < 6; i++)
        {
            mobs.holder.POKEMOBS[i] = data.register(new Data_ItemStack(), ItemStack.EMPTY);
        }

        for (ICapabilityProvider p : event.getCapabilities().values())
        {
            if (p.hasCapability(IAIMob.THUTMOBAI, null)) return;
        }
        AIStuffHolder aiHolder = new AIStuffHolder((MobEntity) event.getObject());
        event.addCapability(AISTUFFCAP, aiHolder);
    }

    private DataSync getData(AttachCapabilitiesEvent<Entity> event)
    {
        for (ICapabilityProvider provider : event.getCapabilities().values())
        {
            if (provider.hasCapability(SyncHandler.CAP, null)) return provider.getCapability(SyncHandler.CAP, null);
        }
        return null;
    }

    private boolean hasCap(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(POKEMOBSCAP)) return true;
        for (ICapabilityProvider provider : event.getCapabilities().values())
        {
            if (provider.hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null)) return true;
        }
        return false;
    }

    @SubscribeEvent
    /** Initializes the AI for the trainers when they join the world.
     * 
     * @param event */
    public void onJoinWorld(EntityJoinWorldEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity npc = (LivingEntity) event.getEntity();
        IHasPokemobs mobs = CapabilityHasPokemobs.getHasPokemobs(npc);
        if (mobs == null || !npc.hasCapability(IAIMob.THUTMOBAI, null)) return;
        IAIMob mob = npc.getCapability(IAIMob.THUTMOBAI, null);

        // Wrap it as a fake vanilla AI
        if (npc instanceof MobEntity)
        {
            mob.setWrapped(true);
            MobEntity living = (MobEntity) npc;
            living.tasks.addTask(0, new EntityAIBaseManager(mob, npc));
        }

        // All can battle, but only trainers will path during battle.
        mob.getAI().addAITask(new AIBattle(npc, !(npc instanceof EntityTrainer)).setPriority(0));

        // All attack zombies.
        mob.getAI().addAITask(new AIFindTarget(npc, EntityZombie.class).setPriority(20));
        // Only trainers specifically target players.
        if (npc instanceof EntityTrainerBase)
        {
            mob.getAI().addAITask(new AIFindTarget(npc, PlayerEntity.class).setPriority(10));
            mob.getAI().addAITask(new AIMate(npc, ((EntityTrainerBase) npc).getClass()));
        }
        // 5% chance of battling a random nearby pokemob if they see it.
        if (Config.instance.trainersBattlePokemobs)
            mob.getAI().addAITask(new AIFindTarget(npc, 0.05f, EntityPokemob.class).setPriority(20));

        // 1% chance of battling another of same class if seen
        if (Config.instance.trainersBattleEachOther)
            mob.getAI().addAITask(new AIFindTarget(npc, 0.01f, npc.getClass()).setPriority(20));

        TypeTrainer newType = TypeTrainer.mobTypeMapper.getType(npc, true);
        if (newType == null) return;
        mobs.setType(newType);
        int level = SpawnHandler.getSpawnLevel(npc.getEntityWorld(), Vector3.getNewVector().set(npc),
                Database.getEntry(1));
        TypeTrainer.getRandomTeam(mobs, npc, level, npc.getEntityWorld());
    }

    public static ItemStack fromString(String arg, ICommandSource sender) throws CommandException
    {
        String[] args = arg.split(" ");
        Item item = CommandBase.getItemByText(sender, args[0]);
        int i = 1;
        int j = args.length >= 3 ? CommandBase.parseInt(args[2].trim()) : 0;
        ItemStack itemstack = new ItemStack(item, i, j);
        if (args.length >= 4)
        {
            String s = CommandBase.buildString(args, 3);

            try
            {
                itemstack.setTag(JsonToNBT.getTagFromJson(s));
            }
            catch (NBTException nbtexception)
            {
                throw new CommandException("commands.give.tagError", new Object[] { nbtexception.getMessage() });
            }
        }
        if (args.length >= 2)
            itemstack.setCount(CommandBase.parseInt(args[1].trim(), 1, item.getItemStackLimit(itemstack)));
        return itemstack;
    }
}