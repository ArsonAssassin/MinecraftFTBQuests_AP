package com.ftbap.ftbap;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Mod(modid = FTBArchipelagoMod.MODID, name = FTBArchipelagoMod.NAME, version = FTBArchipelagoMod.VERSION, dependencies = "after:ftbquests")
public class FTBArchipelagoMod {
    public static final String MODID = "ftbarchipelago";
    public static final String NAME = "FTB Archipelago Integration";
    public static final String VERSION = "1.0";

    private static final Logger LOGGER = LogManager.getLogger();

    @Mod.Instance(MODID)
    public static FTBArchipelagoMod instance;

    private QuestManager questManager;
    private ArchipelagoResourceExporter resourceExporter;
    private ArchipelagoClient archipelagoClient;
    private ArchipelagoRewardManager rewardManager;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("FTB Archipelago Integration: Pre-initialization");
        questManager = new QuestManager();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("FTB Archipelago Integration: Initialization");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("FTB Archipelago Integration: Post-initialization");
    }
    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandFTBAPConnect(this));
    }
    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        LOGGER.info("FTB Archipelago Integration: Server started, loading quests");
        questManager.loadQuestsFromFTBQuests();
        
        resourceExporter = new ArchipelagoResourceExporter(questManager);
        File configDir = event.getServer().getWorld(0).getSaveHandler().getWorldDirectory();
        String outputPath = new File(configDir, "ftbquests_world.py").getAbsolutePath();
        
        try {
            String templatePath = extractTemplate(configDir);
            resourceExporter.exportToAPWorld(outputPath, templatePath);
        } catch (Exception e) {
            LOGGER.error("Failed to generate APWorld file", e);
        }

        rewardManager = new ArchipelagoRewardManager();

        MinecraftForge.EVENT_BUS.register(rewardManager);

        QuestCompletionListener questListener = new QuestCompletionListener(archipelagoClient, rewardManager);
        MinecraftForge.EVENT_BUS.register(questListener);
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        if (archipelagoClient != null) {
            archipelagoClient.disconnect();
        }
    }

    public void connectToArchipelago(String host, int port, String slot, String password) throws Exception {
        if (archipelagoClient != null) {
            archipelagoClient.disconnect();
        }

        archipelagoClient = new ArchipelagoClient(host, port, slot, "FTBQuests");
        archipelagoClient.connect();
        archipelagoClient.sendConnect();

        // You might want to add authentication here using the slot and password
        // This depends on how your ArchipelagoClient is implemented

        // Update the reward manager with the new client
        rewardManager.setArchipelagoClient(archipelagoClient);

        LOGGER.info("Connected to Archipelago server at {}:{}", host, 38281);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && archipelagoClient != null) {
            try {
                String message = archipelagoClient.receiveMessage();
                archipelagoClient.processMessage(message);
            } catch (InterruptedException e) {
                LOGGER.error("Error receiving message from Archipelago server", e);
            }
        }
    }

    private String extractTemplate(File configDir) throws Exception {
        File templateFile = new File(configDir, "ftbquests_world_template.py");
        if (!templateFile.exists()) {
            try (InputStream is = getClass().getResourceAsStream("/ftbquests_world_template.py")) {
                Files.copy(is, templateFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return templateFile.getAbsolutePath();
    }
}