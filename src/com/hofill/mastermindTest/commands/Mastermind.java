package com.hofill.mastermindTest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Mastermind implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("mastermind")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this command!");
			} else {
				if (args.length == 0) {
					sender.sendMessage(ChatColor.BLUE
							+ "In order to create a new mastermind game, you have to create it's layout first. "
							+ "It needs to have a place to display the solution, "
							+ "a place to add the guess and 8/10/12 rows of 4/6/8 pegs");
				} else if (args.length == 2) {
					if(args[0].equalsIgnoreCase("play")) {
						
					}
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("edit")) {
						sender.sendMessage(ChatColor.RED + "Correct usage is /mastermind edit <state>");
					} else if (args[0].equalsIgnoreCase("play")) {
						sender.sendMessage(ChatColor.RED + "Correct usage is /mastermind play <gameId>");
					} else {
						sender.sendMessage(ChatColor.RED + "Correct usage is /mastermind <option> <option>");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Correct usage is /mastermind <option> <option>");
				}
			}
		}
		return true;
	}

}
