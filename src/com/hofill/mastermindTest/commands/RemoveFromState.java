package com.hofill.mastermindTest.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;

import com.hofill.mastermindTest.mysql.MySQL;

public class RemoveFromState implements CommandExecutor {

	MySQL db = new MySQL();
	ArrayList<String> creationState = new ArrayList<String>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (label.equalsIgnoreCase("removefromstate")) {
			try {
				Connection conn = db.openConnection();
				ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM current_state WHERE state = 'creation_state'");
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
						Connection conn = db.openConnection();
						PreparedStatement ps = conn.prepareStatement(
								"DELETE FROM current_state WHERE state = 'creation_state' AND player = ?");
						ps.setString(1, args[0]);
						ps.executeUpdate();
						conn.close();
					} catch (Exception ex) {
					}
					sender.sendMessage(ChatColor.DARK_GRAY + "User " + args[0] + " has been removed from the list!");
				}
			}
		}

		return true;
	}

}
