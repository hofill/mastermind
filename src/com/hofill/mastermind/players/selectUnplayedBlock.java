package com.hofill.mastermind.players;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.hofill.mastermind.inventories.InventoryGuessNumber;
import com.hofill.mastermind.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class selectUnplayedBlock implements Listener {

	MySQL db = new MySQL();
	InventoryGuessNumber inv = new InventoryGuessNumber();
	ArrayList<String> creationState = new ArrayList<String>(); // Unplayed Block
	ArrayList<String> blockStateOne = new ArrayList<String>(); // Correct position
	ArrayList<String> blockStateTwo = new ArrayList<String>(); // Wrong position
	ArrayList<String> beforeSolution = new ArrayList<String>(); // Before solution
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
		beforeSolution.clear();
		// Get states from database
		creationState = getState("creation_state");
		blockStateOne = getState("block_state_one");
		blockStateTwo = getState("block_state_two");
		gameLengthState = getState("game_length_state");
		pegsCountState = getState("pegs_count_state");
		beforeSolution = getState("before_solution_state");
		if (creationState.contains(player.getName())) { // Initial state
			Item item = event.getItemDrop();
			Material mat = item.getItemStack().getType();
			if (mat.isBlock() && !mat.hasGravity()) {
				updateState("creation_state", "block_state_one", player.getName());
				updateGame("unplayed_material", player.getName(), mat.name());
				player.sendMessage(ChatColor.BLUE + "Block selected!");
				player.sendMessage(ChatColor.BLUE
						+ "Drop a block to select the block type for the right position and right peg color state.");
			} else {
				player.sendMessage(ChatColor.DARK_GRAY + "Can't use this block! Please try another one.");
			}
			event.setCancelled(true);
		} else if (blockStateOne.contains(player.getName())) { // State one
			Item item = event.getItemDrop();
			Material mat = item.getItemStack().getType();
			if (mat.isBlock() && !mat.hasGravity()) {
				updateState("block_state_one", "block_state_two", player.getName());
				updateGame("peg_correct_material", player.getName(), mat.name());
				player.sendMessage(ChatColor.BLUE + "Block selected!");
				player.sendMessage(ChatColor.BLUE
						+ "Drop a block to select the block type for the wrong position and right peg color state.");
			} else {
				player.sendMessage(ChatColor.DARK_GRAY + "Can't use this block! Please try another one.");
			}
			event.setCancelled(true);
		} else if (blockStateTwo.contains(player.getName())) { // State two
			Item item = event.getItemDrop();
			Material mat = item.getItemStack().getType();
			if (mat.isBlock() && !mat.hasGravity()) {
				updateState("block_state_two", "before_solution_state", player.getName());
				updateGame("peg_wrong_material", player.getName(), mat.name());
				player.sendMessage(ChatColor.BLUE + "Block selected!");
				player.sendMessage(ChatColor.BLUE + "Drop a block to select the block type for win display.");
			} else {
				player.sendMessage(ChatColor.DARK_GRAY + "Can't use this block! Please try another one.");
			}
			event.setCancelled(true);
		} else if (beforeSolution.contains(player.getName())) {
			Item item = event.getItemDrop();
			Material mat = item.getItemStack().getType();
			if (mat.isBlock() && !mat.hasGravity()) {
				updateState("before_solution_state", "game_length_state", player.getName());
				updateGame("before_solution_material", player.getName(), mat.name());
				player.sendMessage(ChatColor.BLUE + "Block selected!");
				player.sendMessage(ChatColor.BLUE + "Right click on the wand to set the game length.");
				player.openInventory(InventoryGuessNumber.inventory);
			} else {
				player.sendMessage(ChatColor.DARK_GRAY + "Can't use this block! Please try another one.");
			}
			event.setCancelled(true);
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

	private void updateGame(String column, String player, String block) {
		int gameId = getGameId(player);
		try {
			PreparedStatement ps = db.getConnection().prepareStatement("UPDATE games SET " + column + " = ? WHERE game_id = ?");
			ps.setString(1, block);
			ps.setInt(2, gameId);
			ps.executeUpdate();
		} catch (Exception ex) {
		}
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

	private int getGameId(String player) {
		int gameId = 0;
		try {
			PreparedStatement ps = db.getConnection().prepareStatement("SELECT game_id FROM current_state WHERE player = ?");
			ps.setString(1, player);
			ResultSet rs = ps.executeQuery();
			rs.next();
			gameId = rs.getInt(1);
		} catch (Exception ex) {
		}
		return gameId;
	}

}
