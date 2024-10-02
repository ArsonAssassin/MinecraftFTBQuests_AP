package com.ftbap.ftbap;

import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = "ftbap")
public class QuestCompletionListener {
    private static ArchipelagoClient archipelagoClient;
    private static ArchipelagoRewardManager rewardManager;

    public static void init(ArchipelagoClient client, ArchipelagoRewardManager manager) {
        archipelagoClient = client;
        rewardManager = manager;
        }

    @SubscribeEvent
    public static void onQuestCompleted(CustomTaskEvent.Completed event) {
        Quest quest = event.getTask().getQuest();
        String questTitle = quest.getDisplayName().getUnformattedText();
        EntityPlayerMP player = event.getPlayer();

        if (rewardManager.shouldPreventNormalReward(quest, player)) {
            event.setCanceled(true); // Prevent normal reward distribution
            archipelagoClient.sendLocationChecks(questTitle);
        }
    }
}