package com.ftbap.ftbap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ArchipelagoClient {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String host;
    private final int port;
    private final String playerName;
    private final String gameName;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson;
    private final BlockingQueue<String> messageQueue;
    private Thread listenerThread;
    private final ArchipelagoRewardManager rewardManager;

    public ArchipelagoClient(String host, int port, String playerName, String gameName, ArchipelagoRewardManager rewardManager) {
        this.host = host;
        this.port = port;
        this.playerName = playerName;
        this.gameName = gameName;
        this.gson = new Gson();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.rewardManager = rewardManager;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        LOGGER.info("Connected to Archipelago server at {}:{}", host, port);
        startListenerThread();
    }

    private void startListenerThread() {
        listenerThread = new Thread(() -> {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    messageQueue.put(inputLine);
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.error("Error in listener thread", e);
            }
        });
        listenerThread.start();
    }

    public void disconnect() {
        try {
            if (listenerThread != null) {
                listenerThread.interrupt();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            LOGGER.info("Disconnected from Archipelago server");
        } catch (IOException e) {
            LOGGER.error("Error disconnecting from Archipelago server", e);
        }
    }

    public void sendConnect() {
        JsonObject connectPacket = new JsonObject();
        connectPacket.addProperty("cmd", "Connect");
        connectPacket.addProperty("game", gameName);
        connectPacket.addProperty("name", playerName);
        connectPacket.addProperty("uuid", ""); // You might want to generate a UUID for the player
        connectPacket.addProperty("version", "0.1.0"); // Update this with your mod's version

        sendPacket(connectPacket);
    }

    public void sendLocationChecks(String... locationNames) {
        JsonObject locationCheckPacket = new JsonObject();
        locationCheckPacket.addProperty("cmd", "LocationChecks");
        locationCheckPacket.add("locations", gson.toJsonTree(locationNames));

        sendPacket(locationCheckPacket);
    }

    private void sendPacket(JsonObject packet) {
        out.println(gson.toJson(packet));
    }

    public String receiveMessage() throws InterruptedException {
        return messageQueue.take();
    }

    public void processMessage(String message) {
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
        String cmd = jsonMessage.get("cmd").getAsString();

        switch (cmd) {
            case "RoomInfo":
                handleRoomInfo(jsonMessage);
                break;
            case "ReceivedItems":
                handleReceivedItems(jsonMessage);
                break;
            // Add more cases for other message types as needed
            default:
                LOGGER.info("Received unknown command: {}", cmd);
        }
    }

    private void handleRoomInfo(JsonObject roomInfo) {
        // Process room information
        LOGGER.info("Received room info");
    }

    private void handleReceivedItems(JsonObject receivedItems) {
        LOGGER.info("Received items: {}", receivedItems);
        JsonArray items = receivedItems.getAsJsonArray("items");
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            String itemName = item.get("item").getAsString();
            int player = item.get("player").getAsInt();
            
            EntityPlayerMP entityPlayer = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
            if (entityPlayer != null) {
                rewardManager.queueRewardFromArchipelago(itemName, entityPlayer);
            } else {
                LOGGER.warn("Player not found for received item: {}", itemName);
            }
        }
    }
}