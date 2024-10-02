// File: src/main/java/com/ftbap/ftbap/FTBArchipelagoMod.java

package com.ftbap.ftbap;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Mod(modid = FTBArchipelagoMod.MODID, name = FTBArchipelagoMod.NAME, version = FTBArchipelagoMod.VERSION, dependencies = "after:ftbquests")
public class FTBArchipelagoMod {
    public static final String MODID = "ftbap";
    public static final String NAME = "FTB Archipelago Integration";
    public static final String VERSION = "1.0";

    private static final Logger LOGGER = LogManager.getLogger();

    @Mod.Instance(MODID)
    public static FTBArchipelagoMod instance;

    private QuestManager questManager;
    private ArchipelagoResourceExporter resourceExporter;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("FTB Archipelago Integration: Pre-initialization");
        questManager = new QuestManager();
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