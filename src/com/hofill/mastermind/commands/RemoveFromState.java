package com.hofill.mastermind.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.hofill.mastermind.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class RemoveFromState implements CommandExecutor {

	MySQL db = new MySQL();
	ArrayList<String> creationState = new ArrayList<String>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (label.equalsIgnoreCase("removefromstate")) {
			try {
				ResultSet rs = db.getConnection().createStatement().executeQuery("SELECT * FROM current_state");
				creationState.clear();
				while (rs.next()) {
					creationState.add(rs.getString(3));
				}
			} catch (Exception ex) {
			}
			if (args.length != 1) {
				if (args.length == 0) {
					if (!creationState.isEmpty()) {
						sender.sendMessage(
								ChatColor.DARK_GRAY + "" + ChatColor.UNDERLINE + "Current people creating a game:");
						sender.sendMessage("");
						for (String name : creationState) {
							sender.sendMessage(ChatColor.DARK_GRAY + name);
						}
					} else
						sender.sendMessage(ChatColor.DARK_GRAY + "No one is creating a game right now.");
				} else
					sender.sendMessage(ChatColor.DARK_RED + "Correct usage: /removefromstate <name>");
			} else {
				if (!(creationState.contains(args[0]))) {
					sender.sendMessage(ChatColor.DARK_RED + "User " + args[0] + " is not currently creating a game!");
				} else {
					try {
						PreparedStatement ps1 = db.getConnection()
								.prepareStatement("SELECT game_id FROM current_state WHERE player = ?");
						ps1.setString(1, args[0]);
						ResultSet rs = ps1.executeQuery();
						while(rs.next()) {
							db.getConnection().createStatement().executeUpdate("DELETE FROM games WHERE game_id = '" + rs.getInt(1) + "'");
						}
						PreparedStatement ps2 = db.getConnection().prepareStatement("DELETE FROM current_state WHERE player = ?");
						ps2.setString(1, args[0]);
						ps2.executeUpdate();
					} catch (Exception ex) {
					}
					sender.sendMessage(ChatColor.DARK_GRAY + "User " + args[0] + " has been removed from the list!");
				}
			}
		}

		return true;
	}

}
