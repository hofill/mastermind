package com.hofill.mastermindTest.players;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.hofill.mastermindTest.main;
import com.hofill.mastermindTest.inventories.InventoryGuessNumber;
import com.hofill.mastermindTest.inventories.InventoryPegsCount;
import com.hofill.mastermindTest.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class clickInInventory implements Listener{

	MySQL db = new MySQL();
	ArrayList<String> gameLengthState = new ArrayList<String>(); // Game length
	ArrayList<String> pegsCountState = new ArrayList<String>(); // Number of pegs

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().equals(InventoryGuessNumber.inventory)) {
			Player player = (Player) event.getWhoClicked();
			// Clear array
			gameLengthState.clear();
			// Get states from database
			gameLengthState = getState("game_length_state");
			if(gameLengthState.contains(player.getName())) {
				ItemStack greenWool = getWoolGuesses("12 Guesses", Material.GREEN_WOOL, ChatColor.GREEN);
				ItemStack orangeWool = getWoolGuesses("10 Guesses", Material.ORANGE_WOOL, ChatColor.GOLD);
				ItemStack redWool = getWoolGuesses("8 Guesses", Material.RED_WOOL, ChatColor.RED);
				if (event.getCurrentItem() != null
						&& event.getCurrentItem().equals(greenWool)) {
					event.setCancelled(true);
					player.closeInventory();
					updateGame("game_length", player.getName(), 12);
					updateState("game_length_state", "pegs_count_state", player.getName());
					player.sendMessage(ChatColor.BLUE + "12 Selected.");
					player.openInventory(InventoryPegsCount.inventory);
				}
				else if(event.getCurrentItem() != null
						&& event.getCurrentItem().equals(orangeWool)) {
					event.setCancelled(true);
					player.closeInventory();
					updateGame("game_length", player.getName(), 10);
					updateState("game_length_state", "pegs_count_state", player.getName());
					player.sendMessage(ChatColor.BLUE + "10 Selected.");
					player.openInventory(InventoryPegsCount.inventory);
				}
				else if(event.getCurrentItem() != null
						&& event.getCurrentItem().equals(redWool)) {
					event.setCancelled(true);
					player.closeInventory();
					updateGame("game_length", player.getName(), 8);
					updateState("game_length_state", "pegs_count_state", player.getName());
					player.sendMessage(ChatColor.BLUE + "8 Selected.");
					player.openInventory(InventoryPegsCount.inventory);
				}
			}
			new BukkitRunnable() {
		        @Override
		        public void run () {
		        	player.updateInventory();
		        }
		    }.runTaskLater(main.getPlugin(main.class), 2);
		}
		else if(event.getInventory().equals(InventoryPegsCount.inventory)) {
			Player player = (Player) event.getWhoClicked();
			// Clear array
			pegsCountState.clear();
			// Get states from database
			pegsCountState = getState("pegs_count_state");
			if(pegsCountState.contains(player.getName())) {
				ItemStack greenWool = getWoolGuesses("Length 4", Material.GREEN_WOOL, ChatColor.GREEN, Arrays.asList("4 colors per guess", "Default"));
				ItemStack orangeWool = getWoolGuesses("Length 6", Material.ORANGE_WOOL, ChatColor.GOLD, Arrays.asList("6 colors per guess"));
				ItemStack redWool = getWoolGuesses("Length 8", Material.RED_WOOL, ChatColor.RED, Arrays.asList("8 colors per guess"));
				if (event.getCurrentItem() != null
						&& event.getCurrentItem().equals(greenWool)) {
					event.setCancelled(true);
					player.closeInventory();
					updateGame("pegs_count", player.getName(), 4);
					updateState("pegs_count_state", "coord_win_display_state", player.getName());
					player.sendMessage(ChatColor.BLUE + "4 Selected.");
				}
				else if(event.getCurrentItem() != null
						&& event.getCurrentItem().equals(orangeWool)) {
					event.setCancelled(true);
					player.closeInventory();
					updateGame("pegs_count", player.getName(), 6);
					updateState("pegs_count_state", "coord_win_display_state", player.getName());
					player.sendMessage(ChatColor.BLUE + "6 Selected.");
				}
				else if(event.getCurrentItem() != null
						&& event.getCurrentItem().equals(redWool)) {
					event.setCancelled(true);
					player.closeInventory();
					updateGame("pegs_count", player.getName(), 8);
					updateState("pegs_count_state", "coord_win_display_state", player.getName());
					player.sendMessage(ChatColor.BLUE + "8 Selected.");
				}
			}
			new BukkitRunnable() {
		        @Override
		        public void run () {
		        	player.updateInventory();
		        }
		    }.runTaskLater(main.getPlugin(main.class), 2);
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

	private void updateGame(String column, String player, int amount) {
		int gameId = getGameId(player);
		try {
			Connection conn = db.openConnection();
			PreparedStatement ps = conn.prepareStatement("UPDATE games SET " + column + " = ? WHERE game_id = ?");
			ps.setInt(1, amount);
			ps.setInt(2, gameId);
			ps.executeUpdate();
			conn.close();
		} catch (Exception ex) {
		}
	}

	private void updateState(String initialState, String changedState, String player) {
		try {
			Connection conn = db.openConnection();
			PreparedStatement ps = conn.prepareStatement("UPDATE current_state SET state = ? WHERE state = ? AND player = ?");
			ps.setString(1, changedState);
			ps.setString(2, initialState);
			ps.setString(3, player);
			ps.executeUpdate();
			conn.close();
		} catch (Exception ex) {
		}
	}

	private int getGameId(String player) {
		int gameId = 0;
		try {
			Connection conn = db.openConnection();
			PreparedStatement ps = conn
					.prepareStatement("SELECT game_id FROM current_state WHERE player = ?");
			ps.setString(1, player);
			ResultSet rs = ps.executeQuery();
			rs.next();
			gameId = rs.getInt(1);
			conn.close();
		} catch (Exception ex) {
		}
		return gameId;
	}

	private ItemStack getWoolGuesses(String guesses, Material material, ChatColor color) {
		ItemStack guessItem = new ItemStack(material);
		ItemMeta guessItemMeta = guessItem.getItemMeta();
		guessItemMeta.setDisplayName(color + guesses);
		guessItem.setItemMeta(guessItemMeta);
		return guessItem;
	}
	
	private static ItemStack getWoolGuesses(String guesses, Material material, ChatColor color, List<String> lore) {
		ItemStack guessItem = new ItemStack(material);
		ItemMeta guessItemMeta = guessItem.getItemMeta();
		guessItemMeta.setDisplayName(color + guesses);
		guessItemMeta.setLore(lore);
		guessItem.setItemMeta(guessItemMeta);
		return guessItem;
	}

}
