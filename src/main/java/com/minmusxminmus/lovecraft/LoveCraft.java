package com.minmusxminmus.lovecraft;

import com.minmusxminmus.lovecraft.content.collections.Blocks;
import com.minmusxminmus.lovecraft.content.collections.Items;
import com.minmusxminmus.lovecraft.proxy.Proxies;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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

    private static Logger logger;

    @Mod.Instance(MOD_ID)
    public static LoveCraft INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) { }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        blocks.getAll().forEach(block -> {
            logger.info("Registering block \"" + block.getRegistryName() + "\"");
            event.getRegistry().register(block);
        });
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        items.getAll().forEach(item -> {
            logger.info("Registering item \"" + item.getRegistryName() + "\"");
            event.getRegistry().register(item);
        });
        blocks.getAllHasItem().forEach(block -> {
            logger.info("Registering block item \"" + block.getItem().getRegistryName() + "\"");
            event.getRegistry().register(block.getItem());
        });
    }

    @SubscribeEvent
    public static void registerItemModels(ModelRegistryEvent event) {
        items.getAll().forEach(item -> {
            logger.info("Registering model for item \"" + item.getRegistryName() + "\" with metadata \"0\"");
            Proxies.models.register(item, 0);
        });
        blocks.getAllHasItem().forEach(block -> {
            logger.info("Registering model for block item \"" + block.getItem().getRegistryName() + "\" with metadata \"0\"");
            Proxies.models.register(Item.getItemFromBlock((Block) block), 0);
        });
    }
}
