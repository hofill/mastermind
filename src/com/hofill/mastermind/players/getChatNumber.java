package com.hofill.mastermind.players;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.hofill.mastermind.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class getChatNumber implements Listener {

	MySQL db = new MySQL();
	ArrayList<String> buttonAmountState = new ArrayList<String>(); // Button amount

	@EventHandler
	public void onChatNumber(AsyncPlayerChatEvent event) {
		if (isInt(event.getMessage())) {
			Player player = event.getPlayer();
			buttonAmountState.clear();
			buttonAmountState = getState("button_amount_state");
			if (buttonAmountState.contains(player.getName())) {
				int number = Integer.parseInt(event.getMessage());
				if (number > 0 && number < 5) {
					updateGame("button_amount", player.getName(), number);
					player.sendMessage(ChatColor.BLUE + "" + number + " selected.");
					updateState("button_amount_state", "button_coord_state", player.getName());
					player.sendMessage(ChatColor.BLUE + "Right click on the buttons that confirm the guess.");
					event.setCancelled(true);
				} else {
					player.sendMessage(ChatColor.RED + "Type a number between 1 and 4.");
					event.setCancelled(true);
				}
			}
		}
	}

	private ArrayList<String> getState(String state) {
		ArrayList<String> stateArray = new ArrayList<String>();
		try {
			ResultSet rs = db.getConnection().createStatement()
					.executeQuery("SELECT * FROM current_state WHERE state = '" + state + "'");
			stateArray.clear();
			while (rs.next()) {
				stateArray.add(rs.getString(3));
			}
		} catch (Exception ex) {
		}
		return stateArray;
	}

	private void updateGame(String column, String player, int change) {
		int gameId = getGameId(player);
		try {
			db.getConnection().createStatement()
					.executeUpdate("UPDATE games SET " + column + " = " + change + " WHERE game_id = " + gameId);
		} catch (Exception ex) {
		}
	}

	private int getGameId(String player) {
		int gameId = 0;
		try {
			PreparedStatement ps = db.getConnection()
					.prepareStatement("SELECT game_id FROM current_state WHERE player = ?");
			ps.setString(1, player);
			ResultSet rs = ps.executeQuery();
			rs.next();
			gameId = rs.getInt(1);
		} catch (Exception ex) {
		}
		return gameId;
	}

	private void updateState(String initialState, String changedState, String player) {
		try {
			PreparedStatement ps = db.getConnection()
					.prepareStatement("UPDATE current_state SET state = ? WHERE state = ? AND player = ?");
			ps.setString(1, changedState);
			ps.setString(2, initialState);
			ps.setString(3, player);
			ps.executeUpdate();
		} catch (Exception ex) {
		}
	}

	private boolean isInt(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (Exception ex) {
		}
		return false;
	}

}
