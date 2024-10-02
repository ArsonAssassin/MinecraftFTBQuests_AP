package com.ftbap.ftbap;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandFTBAPConnect extends CommandBase {
    private final FTBArchipelagoMod mod;

    public CommandFTBAPConnect(FTBArchipelagoMod mod) {
        this.mod = mod;
    }

    @Override
    public String getName() {
        return "ftb_ap";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ftb_ap connect <host> <port> <slot> <password>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || !args[0].equals("connect")) {
            throw new CommandException("Invalid command. Usage: " + getUsage(sender));
        }

        if (args.length != 5) {  // Changed from 4 to 5 to match the usage
            throw new CommandException("Invalid number of arguments. Usage: " + getUsage(sender));
        }

        String host = args[1];
        int port;
        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid port number. Must be an integer.");
        }
        String slot = args[3];
        String password = args[4];

        try {
            mod.connectToArchipelago(host, port, slot, password);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Successfully connected to Archipelago server."));
        } catch (Exception e) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to connect to Archipelago server: " + e.getMessage()));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Require permission level 2 (default for ops)
    }
}