package com.ftbap.ftbap;

import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArchipelagoRewardManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ArchipelagoClient archipelagoClient;
    private final Map<UUID, Map<String, ItemStack>> pendingRewards;

    public ArchipelagoRewardManager(ArchipelagoClient archipelagoClient) {
        this.archipelagoClient = archipelagoClient;
        this.pendingRewards = new HashMap<>();
    }

    public boolean shouldPreventNormalReward(Quest quest, EntityPlayerMP player) {
        // Check if this quest's rewards should be managed by Archipelago
        return true; // For now, we'll assume all quests are part of the randomizer
    }

    public void queueRewardFromArchipelago(String itemName, EntityPlayerMP player) {
        // This method would be called when we receive an item from Archipelago
        ItemStack itemStack = createItemStackFromName(itemName);
        UUID playerUUID = player.getUniqueID();
        pendingRewards.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(itemName, itemStack);
    }

    public void grantPendingRewards(EntityPlayerMP player) {
        UUID playerUUID = player.getUniqueID();
        Map<String, ItemStack> playerRewards = pendingRewards.get(playerUUID);
        if (playerRewards != null) {
            for (ItemStack itemStack : playerRewards.values()) {
                if (!player.inventory.addItemStackToInventory(itemStack)) {
                    player.dropItem(itemStack, false);
                }
            }
            pendingRewards.remove(playerUUID);
        }
    }

    private ItemStack createItemStackFromName(String itemName) {
        // Convert the item name to an actual ItemStack
        // This is a placeholder and needs to be implemented based on your item naming convention
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            grantPendingRewards((EntityPlayerMP) event.player);
        }
    }

    public void setArchipelagoClient(ArchipelagoClient client) {
        this.archipelagoClient = client;
    }
}