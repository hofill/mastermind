package com.hofill.mastermindTest.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hofill.mastermindTest.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class Setup implements CommandExecutor {

	MySQL db = new MySQL();
	ArrayList<String> creationState = new ArrayList<String>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (label.equalsIgnoreCase("setupgame")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this command!");
				return false;
			} else {
				creationState.clear();
				creationState = getState("creation_state");
				Player player = (Player) sender;
				if (creationState.contains(player.getName())) {
					sender.sendMessage(ChatColor.DARK_RED
							+ "You are already creating a game! If not, use /removefromstate <name>");
				} else if (creationState.size() > 1) {
					sender.sendMessage(ChatColor.DARK_RED + "Too many people are creating a game! Try again later.");
				} else {
					try {
						Connection conn = db.openConnection();
						PreparedStatement ps = conn
								.prepareStatement("INSERT INTO current_state(state,player) VALUES(?,?)");
						ps.setString(1, "creation_state");
						ps.setString(2, player.getName());
						ps.executeUpdate();
						conn.close();
					} catch (Exception ex) {
					}
					ItemStack createGame = new ItemStack(Material.BLAZE_ROD, 1);
					createGame.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
					ItemMeta metaCreateGame = createGame.getItemMeta();
					metaCreateGame.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&2Create Game"));
					createGame.setItemMeta(metaCreateGame);
					player.getInventory().addItem(createGame);
					sender.sendMessage(ChatColor.BLUE + "Make sure you are facing north!");
					// Phase one - Select unplayed block
					sender.sendMessage(ChatColor.BLUE + "Drop a block to select the block type for the unplayed rows"); 
				}
			}
		}

		return true;
	}
	
	private ArrayList<String> getState(String state){
		ArrayList<String> stateArray = new ArrayList<String>();
		try {	
			Connection conn = db.openConnection();
			ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM current_state WHERE state = '" + state + "'");
			stateArray.clear();
			while (rs.next()) {
				stateArray.add(rs.getString(3));
			}
		} catch (Exception ex) {
		}
		return stateArray;
		
	}

}
