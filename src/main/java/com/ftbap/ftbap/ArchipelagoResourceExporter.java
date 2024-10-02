package com.ftbap.ftbap;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ArchipelagoResourceExporter {
    private static final Logger LOGGER = LogManager.getLogger();
    private final QuestManager questManager;
    private final Gson gson;

    public ArchipelagoResourceExporter(QuestManager questManager) {
        this.questManager = questManager;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void exportToAPWorld(String outputFilePath, String templateFilePath) {
        Map<String, Integer> itemNameToId = createItemNameToIdMap();
        Map<String, Integer> locationNameToId = createLocationNameToIdMap();

        try {
            String template = readTemplateFile(templateFilePath);
            String output = replaceInTemplate(template, itemNameToId, locationNameToId);

            try (FileWriter writer = new FileWriter(outputFilePath)) {
                writer.write(output);
            }

            LOGGER.info("APWorld file created successfully at: " + outputFilePath);
        } catch (IOException e) {
            LOGGER.error("Error creating APWorld file", e);
        }
    }

    private String readTemplateFile(String templateFilePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(templateFilePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private String replaceInTemplate(String template, Map<String, Integer> itemNameToId, Map<String, Integer> locationNameToId) {
        String itemNameToIdJson = gson.toJson(itemNameToId);
        String locationNameToIdJson = gson.toJson(locationNameToId);

        return template.replace("{ITEM_NAME_TO_ID_PLACEHOLDER}", itemNameToIdJson)
                       .replace("{LOCATION_NAME_TO_ID_PLACEHOLDER}", locationNameToIdJson);
    }

    private Map<String, Integer> createItemNameToIdMap() {
        Map<String, Integer> itemMap = new HashMap<>();
        int id = 0;
        for (Quest quest : questManager.getAllQuests()) {
            for (Reward reward : quest.getRewards()) {
                if (reward instanceof ItemReward) {
                    ItemReward itemReward = (ItemReward) reward;
                    ItemStack stack = itemReward.getStack();
                    String itemName = stack.getItem().getRegistryName().toString();
                    if (!itemMap.containsKey(itemName)) {
                        itemMap.put(itemName, id++);
                    }
                }
            }
        }
        return itemMap;
    }

    private Map<String, Integer> createLocationNameToIdMap() {
        Map<String, Integer> locationMap = new HashMap<>();
        int id = 0;
        for (Quest quest : questManager.getAllQuests()) {
            locationMap.put(quest.getDisplayName().getUnformattedText(), id++);
        }
        return locationMap;
    }
}