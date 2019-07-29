package com.hofill.mastermindTest.players;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hofill.mastermindTest.inventories.InventoryGuessNumber;
import com.hofill.mastermindTest.inventories.InventoryPegsCount;
import com.hofill.mastermindTest.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class rightClickWand implements Listener {

	MySQL db = new MySQL();
	ArrayList<String> gameLengthState = new ArrayList<String>(); // Game length
	ArrayList<String> pegsCountState = new ArrayList<String>(); // Number of pegs

	@EventHandler
	public void onRightClickWand(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack createGame = new ItemStack(Material.BLAZE_ROD, 1);
		createGame.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
		ItemMeta metaCreateGame = createGame.getItemMeta();
		metaCreateGame.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&2Create Game"));
		metaCreateGame.setLore(Arrays.asList("Aids you in creating your Mastermind game"));
		createGame.setItemMeta(metaCreateGame);
		ItemStack eventItem = event.getItem();
		if(eventItem != null) createGame.setAmount(eventItem.getAmount());
		if (eventItem != null && eventItem.equals(createGame)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			// Clear arrays
			gameLengthState.clear();
			pegsCountState.clear();
			// Get states from database
			gameLengthState = getState("game_length_state");
			pegsCountState = getState("pegs_count_state");
			if (gameLengthState.contains(player.getName())) {
				player.openInventory(InventoryGuessNumber.inventory);
			}
			else if (pegsCountState.contains(player.getName())) {
				player.openInventory(InventoryPegsCount.inventory);
			}
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

}
