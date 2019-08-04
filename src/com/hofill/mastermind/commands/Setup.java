package com.hofill.mastermind.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hofill.mastermind.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class Setup implements CommandExecutor {

	MySQL db = new MySQL();
	ArrayList<String> creationState = new ArrayList<String>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (label.equalsIgnoreCase("setupgame")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this command!");
			} else {
				creationState.clear();
				creationState = getState();
				Player player = (Player) sender;
				if (creationState.contains(player.getName())) {
					sender.sendMessage(ChatColor.DARK_RED
							+ "You are already creating a game! If not, use /removefromstate <name>");
				} else if (creationState.size() > 1) {
					sender.sendMessage(ChatColor.DARK_RED + "Too many people are creating a game! Try again later.");
				} else {
					try {
						PreparedStatement ps = db.getConnection()
								.prepareStatement("INSERT INTO current_state(state,player,game_id,is_editing) VALUES(?,?,?,?)");
						ps.setString(1, "creation_state");
						ps.setString(2, player.getName());
						ps.setInt(3, getGameId());
						ps.setBoolean(4, false);
						ps.executeUpdate();
					} catch (Exception ex) {
					}
					ItemStack createGame = new ItemStack(Material.BLAZE_ROD, 1);
					createGame.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
					ItemMeta metaCreateGame = createGame.getItemMeta();
					metaCreateGame.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&2Create Game"));
					metaCreateGame.setLore(Arrays.asList("Aids you in creating your Mastermind game"));
					createGame.setItemMeta(metaCreateGame);
					player.getInventory().addItem(createGame);
					// Phase one - Select unplayed block
					sender.sendMessage(ChatColor.BLUE + "Drop a block to select the block type for the unplayed rows.");
				}
			}
		}

		return true;
	}

	private ArrayList<String> getState() {
		ArrayList<String> stateArray = new ArrayList<String>();
		try {
			ResultSet rs = db.getConnection().createStatement().executeQuery("SELECT * FROM current_state");
			stateArray.clear();
			while (rs.next()) {
				stateArray.add(rs.getString(3));
			}
		} catch (Exception ex) {
		}
		return stateArray;
	}

	private int getGameId() {
		int gameId = 0;
		try {
			ResultSet rs = db.getConnection().createStatement().executeQuery("SELECT game_id FROM games ORDER BY game_id");
			if (rs.next()) {
				rs.last();
				gameId = rs.getInt(1) + 1;
			}
			db.getConnection().createStatement().executeUpdate("INSERT INTO games(game_id,is_played) VALUES('" + gameId + "', false)");
		} catch (Exception ex) {
		}
		return gameId;
	}

}
