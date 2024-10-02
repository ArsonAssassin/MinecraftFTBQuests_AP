package com.ftbap.ftbap;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<String, Quest> quests;
    private final Gson gson;

    public QuestManager() {
        quests = new HashMap<>();
        gson = new Gson();
    }

    public void loadQuestsFromFTBQuests() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            LOGGER.error("Failed to get MinecraftServer instance");
            return;
        }

        File worldDirectory = server.getWorld(0).getSaveHandler().getWorldDirectory();
        File ftbQuestsDirectory = new File(worldDirectory, "ftbquests");

        if (!ftbQuestsDirectory.exists() || !ftbQuestsDirectory.isDirectory()) {
            LOGGER.error("FTBQuests directory not found");
            return;
        }

        QuestFile questFile = QuestFile.INSTANCE;
        for (Quest quest : questFile.getAllQuests()) {
            quests.put(quest.getID(), quest);
            LOGGER.info("Loaded quest: " + quest.getDisplayName().getUnformattedText());
        }
    }

    public Quest getQuest(String questId) {
        return quests.get(questId);
    }

    public boolean hasQuest(String questId) {
        return quests.containsKey(questId);
    }

    public List<Quest> getAllQuests() {
        return new ArrayList<>(quests.values());
    }
}