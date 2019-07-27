package com.hofill.mastermindTest.players;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.hofill.mastermindTest.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class selectUnplayedBlock implements Listener {

	MySQL db = new MySQL();
	ArrayList<String> creationState = new ArrayList<String>(); // Unplayed Block
	ArrayList<String> blockStateOne = new ArrayList<String>(); // Correct position
	ArrayList<String> blockStateTwo = new ArrayList<String>(); // Wrong position
	ArrayList<String> gameLengthState = new ArrayList<String>(); // Game length
	ArrayList<String> pegsCountState = new ArrayList<String>(); // Number of pegs

	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		// Clear arrays
		creationState.clear();
		blockStateOne.clear();
		blockStateTwo.clear();
		gameLengthState.clear();
		pegsCountState.clear();
		// Get states from database
		creationState = getState("creation_state");
		blockStateOne = getState("block_state_one");
		blockStateTwo = getState("block_state_two");
		gameLengthState = getState("game_length_state");
		pegsCountState = getState("pegs_count_state");
		if (creationState.contains(player.getName())) { // Initial state
			Item item = event.getItemDrop();
			Material mat = item.getItemStack().getType();
			if (mat.isBlock() && !mat.hasGravity()) {
				removeState("creation_state", player.getName());
				setState("block_state_one", player.getName());
				player.sendMessage(ChatColor.BLUE + "Block selected!");
				player.sendMessage(ChatColor.BLUE
						+ "Drop a block to select the block type for the right position and right peg color state.");
				updateGame("unplayed_material", player.getName(), mat.name(), "creation_state");
			} else {
				player.sendMessage(ChatColor.DARK_GRAY + "Can't use this block! Please try another one.");
			}
			event.setCancelled(true);
		} else if (blockStateOne.contains(player.getName())) { // State one
			Item item = event.getItemDrop();
			Material mat = item.getItemStack().getType();
			if (mat.isBlock() && !mat.hasGravity()) {
				removeState("block_state_one", player.getName());
				setState("block_state_two", player.getName());
				player.sendMessage(ChatColor.BLUE + "Block selected!");
				player.sendMessage(ChatColor.BLUE
						+ "Drop a block to select the block type for the wrong position and right peg color state.");
				updateGame("peg_correct_material", player.getName(), mat.name(), "block_state_one");
			} else {
				player.sendMessage(ChatColor.DARK_GRAY + "Can't use this block! Please try another one.");
			}
			event.setCancelled(true);
		} else if (blockStateTwo.contains(player.getName())) { // State two
			Item item = event.getItemDrop();
			Material mat = item.getItemStack().getType();
			if (mat.isBlock() && !mat.hasGravity()) {
				removeState("block_state_two", player.getName());
				setState("game_length_state", player.getName());
				player.sendMessage(ChatColor.BLUE + "Block selected!");
				player.sendMessage(ChatColor.BLUE + "Click on the chat to choose the amount of allowed guesses.");
				updateGame("peg_wrong_material", player.getName(), mat.name(), "block_state_two");
			} else {
				player.sendMessage(ChatColor.DARK_GRAY + "Can't use this block! Please try another one.");
			}
			event.setCancelled(true);
		}
	}

	private ArrayList<String> getState(String state) {
		ArrayList<String> stateArray = new ArrayList<String>();
		try {
			Connection conn = db.openConnection();
			ResultSet rs = conn.createStatement()
					.executeQuery("SELECT * FROM current_state WHERE state = '" + state + "'");
			stateArray.clear();
			while (rs.next()) {
				stateArray.add(rs.getString(3));
			}
			conn.close();
		} catch (Exception ex) {
		}
		return stateArray;
	}

	private void setState(String state, String player) {
		try {
			Connection conn = db.openConnection();
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO current_state(state,player,game_id) VALUES(?,?,?)");
			ps.setString(1, state);
			ps.setString(2, player);
			ps.setInt(3, getGameId(state, player));
			ps.executeUpdate();
			conn.close();
		} catch (Exception ex) {
		}
	}

	private void updateGame(String column, String player, String block, String state) {
		try {
			Connection conn = db.openConnection();
			PreparedStatement ps = conn.prepareStatement("UPDATE games SET " + column + " = ? WHERE game_id = ?");
			ps.setString(1, block);
			ps.setInt(2, getGameId(state, player));
			ps.executeUpdate();
			conn.close();
		} catch (Exception ex) {
		}
	}

	private void removeState(String state, String player) {
		try {
			Connection conn = db.openConnection();
			PreparedStatement ps = conn.prepareStatement("DELETE FROM current_state WHERE state = ? AND player = ?");
			ps.setString(1, state);
			ps.setString(2, player);
			ps.executeUpdate();
			conn.close();
		} catch (Exception ex) {
		}
	}

	private int getGameId(String state, String player) {
		int gameId = 0;
		try {
			Connection conn = db.openConnection();
			PreparedStatement ps = conn
					.prepareStatement("SELECT game_id FROM current_state WHERE state = ? AND player = ?");
			ps.setString(1, state);
			ps.setString(2, player);
			ResultSet rs = ps.executeQuery();
			rs.next();
			gameId = rs.getInt(1);
			conn.close();
		} catch (Exception ex) {
		}
		return gameId;
	}

}
