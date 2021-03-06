package com.minmusxminmus.lovecraft;

import com.minmusxminmus.lovecraft.content.capabilities.MadnessProvider;
import com.minmusxminmus.lovecraft.content.collections.Blocks;
import com.minmusxminmus.lovecraft.content.collections.Items;
import com.minmusxminmus.lovecraft.content.commands.CommandLoveCraft;
import com.minmusxminmus.lovecraft.content.commands.CommandMadness;
import com.minmusxminmus.lovecraft.content.gui.LoveCraftGuiHandler;
import com.minmusxminmus.lovecraft.content.madness.IMadness;
import com.minmusxminmus.lovecraft.content.madness.PlayerMadness;
import com.minmusxminmus.lovecraft.content.madness.paths.*;
import com.minmusxminmus.lovecraft.proxy.Proxies;
import com.minmusxminmus.lovecraft.util.StringFormatter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.Logger;


@Mod(modid = LoveCraft.MOD_ID, name = LoveCraft.NAME, version = LoveCraft.VERSION)
@Mod.EventBusSubscriber
public class LoveCraft
{
    public static final String MOD_ID = "lovecraft";
    public static final String NAME = "LoveCraft";
    public static final String VERSION = "1.0";

    private static final Items items = Items.INSTANCE;
    private static final Blocks blocks = Blocks.INSTANCE;

    public static Logger LOGGER;

    @Mod.Instance(MOD_ID)
    public static LoveCraft INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();

        LOGGER.info("Registering capabilities");
        Proxies.initialization.registerCapabilities();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Registering custom GUI handler");
        NetworkRegistry.INSTANCE.registerGuiHandler(LoveCraft.INSTANCE, new LoveCraftGuiHandler());
        LOGGER.info("Caching Madness path registry");
        MadnessPath.MADNESS_REGISTRY = GameRegistry.findRegistry(MadnessPath.class);
    }

    @Mod.EventHandler
    public void registerCommands(FMLServerStartingEvent event) {
        LOGGER.info("Registering LoveCraft commands");
        event.registerServerCommand(new CommandLoveCraft());
        event.registerServerCommand(new CommandMadness());
    }

    @SubscribeEvent
    public static void registerRegistries(RegistryEvent.NewRegistry event) {
        RegistryBuilder<MadnessPath> registryBuilder = new RegistryBuilder<>();
        registryBuilder.setType(MadnessPath.class);
        registryBuilder.setName(StringFormatter.asResourceLocation("madnessRegistry"));
        registryBuilder.create();
    }

    @SubscribeEvent
    public static void attachMadnessCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            LOGGER.info("Attaching madness capability to player");
            event.addCapability(StringFormatter.asResourceLocation("madnessCapability"), new MadnessProvider(new PlayerMadness()));
        }
    }

    @SubscribeEvent
    public static void refreshMadnessAfterDeath(PlayerEvent.Clone event) {
        assert IMadness.MADNESS_CAPABILITY != null;
        if (event.isWasDeath()) {
            LOGGER.info("Persisting madness values from recently dead player '" + event.getOriginal().getDisplayNameString() + "'" );
            if (!event.getOriginal().hasCapability(IMadness.MADNESS_CAPABILITY, null)) {
                LOGGER.warn("Old player '" + event.getOriginal().getDisplayNameString() + "' had no madness capability. Ignoring");
                return;
            }
            double madnessValue = event.getOriginal().getCapability(IMadness.MADNESS_CAPABILITY, null).getLevel();
            if (!event.getEntityPlayer().hasCapability(IMadness.MADNESS_CAPABILITY, null)) {
                LOGGER.warn("New player '" + event.getEntityPlayer().getDisplayNameString() + "' has no madness capability. Ignoring");
                return;
            }
            event.getEntityPlayer().getCapability(IMadness.MADNESS_CAPABILITY, null).setLevel(madnessValue);
            event.getEntityPlayer().getCapability(IMadness.MADNESS_CAPABILITY, null).refresh();
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        blocks.getAll().forEach(block -> {
            LOGGER.info("Registering block \"" + block.getRegistryName() + "\"");
            event.getRegistry().register(block);
        });
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        items.getAll().forEach(item -> {
            LOGGER.info("Registering item \"" + item.getRegistryName() + "\"");
            event.getRegistry().register(item);
        });
        blocks.getAllHasItem().forEach(block -> {
            LOGGER.info("Registering block item \"" + block.getItem().getRegistryName() + "\"");
            event.getRegistry().register(block.getItem());
        });
    }

    @SubscribeEvent
    public static void registerMadnessPaths(RegistryEvent.Register<MadnessPath> event) {
        MadnessPath pathScience = new MadnessPathScience();
        MadnessPath pathLore = new MadnessPathLore();
        MadnessPath pathDeep = new MadnessPathDeep();
        MadnessPath pathDream = new MadnessPathDream();
        event.getRegistry().registerAll(
                new MadnessPathScience().setRegistryName(new ResourceLocation(LoveCraft.MOD_ID, pathScience.getIdentifier())),
                new MadnessPathLore().setRegistryName(new ResourceLocation(LoveCraft.MOD_ID, pathLore.getIdentifier())),
                new MadnessPathDeep().setRegistryName(new ResourceLocation(LoveCraft.MOD_ID, pathDeep.getIdentifier())),
                new MadnessPathDream().setRegistryName(new ResourceLocation(LoveCraft.MOD_ID, pathDream.getIdentifier()))
        );
    }

    @SubscribeEvent
    public static void registerItemModels(ModelRegistryEvent event) {
        items.getAll().forEach(item -> {
            LOGGER.info("Registering model for item \"" + item.getRegistryName() + "\" with metadata \"0\"");
            Proxies.models.register(item, 0);
        });
        blocks.getAllHasItem().forEach(block -> {
            LOGGER.info("Registering model for block item \"" + block.getItem().getRegistryName() + "\" with metadata \"0\"");
            Proxies.models.register(Item.getItemFromBlock((Block) block), 0);
        });
    }

    @Config(modid = MOD_ID, name = "LoveCraft")
    public static class Configuration {

        public static class MadnessConfig {

            @Config.Name("Maximum Madness value")
            @Config.Comment({
                    "This determines the maximum Madness value that can be reached by a player.",
                    "It's worth mentioning that this value has to be higher than the \"Minimum Madness value\" config",
                    "Madness events are triggered by this value, so changing it may affect what events can or cannot happen.",
                    "It is recommended to leave this untouched, unless you're playing with an addon that adds events past the default level."
            })
            @Config.RangeDouble(min = 0.0D)
            public double MAX_MADNESS = 100.0D;

            @Config.Name("Minimum Madness value")
            @Config.Comment({
                    "This determines the minimum Madness value that can be reached by a player.",
                    "It's worth mentioning that this value has to be lower than the \"Maximum Madness value\" config",
                    "This value coincides with the madness level a player gets when joining a new world.",
                    "It is recommended to leave this untouched, unless you're playing with an addon that adds events before the default level.",
                    "Note: having this value above any madness event's level means that said event can and will be triggered as if the player had reached the Madness level means."
            })
            @Config.RangeDouble(min = 0.0D)
            public double MIN_MADNESS = 0.0D;
        }

        @Config.Name("Madness configuration")
        @Config.Comment({
                "This section refers to values related to the Madness functionality."
        })
        public static MadnessConfig madnessConfig = new MadnessConfig();

        public static class PathConfig {
            @Config.Name("Deep path enabled")
            @Config.Comment({
                    "This determines whether or not the Deep path is enabled.",
                    "Disabling this means the player won't be able to enter said path, potentially locking them from certain features without the use of cheats."
            })
            public boolean DEEP_PATH_ENABLED = true;

            @Config.Name("Dream path enabled")
            @Config.Comment({
                    "This determines whether or not the Dream path is enabled.",
                    "Disabling this means the player won't be able to enter said path, potentially locking them from certain features without the use of cheats."
            })
            public boolean DREAM_PATH_ENABLED = true;

            @Config.Name("Science path enabled")
            @Config.Comment({
                    "This determines whether or not the Science path is enabled.",
                    "Disabling this means the player won't be able to enter said path, potentially locking them from certain features without the use of cheats."
            })
            public boolean SCIENCE_PATH_ENABLED = true;

            @Config.Name("Knowledge path enabled")
            @Config.Comment({
                    "This determines whether or not the Knowledge path is enabled.",
                    "Disabling this means the player won't be able to enter said path, potentially locking them from certain features without the use of cheats."
            })
            public boolean KNOWLEDGE_PATH_ENABLED = true;
        }
        @Config.Name("Madness path configuration")
        @Config.Comment({
                "This section addresses the configuration related to the madness paths available through the base mod.",
                "Paths added via addons will not have their configuration here - check the addon's config file or mod page for specific information."
        })
        public static PathConfig pathConfig = new PathConfig();
    }
}
