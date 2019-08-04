package com.hofill.mastermind.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.hofill.mastermind.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class Mastermind implements CommandExecutor {

	MySQL db = new MySQL();

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
							+ "a place to add the guess and 8/10/12 rows of 4/6/8 pegs.");
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("play")) {
						if (isInt(args[1])) {
							int gameId = Integer.parseInt(args[1]);
							if (gameExists(gameId)) {
								Player player = (Player) sender;
								if (isInventoryEmpty(player.getInventory())) {
									if (!isOccupied(gameId)) {
										setOccupied(gameId);
										Location location = getLocation(gameId);
										Location toTeleport = player.getLocation();
										String locationTeleport = "" + toTeleport.getBlockX() + ","
												+ toTeleport.getBlockY() + "," + toTeleport.getBlockZ();
										player.teleport(location);
										int pegCount = getPegCount(gameId);
										String answer = generateAnswer(pegCount);
										
										playGame(answer, player.getName(), gameId, locationTeleport);
										String[] items = { "RED", "GREEN", "BLUE", "YELLOW", "BROWN", "ORANGE", "BLACK",
												"WHITE" };
										for (String i : items) {
											ItemStack is = new ItemStack(Material.getMaterial(i + "_WOOL"), 64);
											player.getInventory().addItem(is);
										}
									} else {
										sender.sendMessage(ChatColor.BLUE + "Someone is already playing!");
									}
								} else {
									sender.sendMessage(ChatColor.BLUE + "Your inventory needs to be empty to play!");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Game with id " + args[1] + " does not exist!");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "The game id can only be a number!");
						}
					} else if (args[0].equalsIgnoreCase("leave")) {

					} else if (args[0].equalsIgnoreCase("edit")) {

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

	private boolean isOccupied(int gameId) {
		boolean isPlayed = false;
		try {
			ResultSet rs = db.getConnection().createStatement().executeQuery("SELECT is_played FROM games WHERE game_id = " + gameId);
			rs.next();
			isPlayed = rs.getBoolean(1);
		} catch (Exception ex) {
		}
		return isPlayed;
	}

	private void playGame(String answer, String player, int gameId, String location) {
		try {
			PreparedStatement ps = db.getConnection().prepareStatement(
					"INSERT INTO games_in_progress(game_id,player,answer,at_guess,teleport) VALUES(?,?,?,?,?)");
			ps.setInt(1, gameId);
			ps.setString(2, player);
			ps.setString(3, answer);
			ps.setInt(4, 0);
			ps.setString(5, location);
			ps.executeUpdate();
		} catch (Exception ex) {
		}
	}

	private int getPegCount(int gameId) {
		int pegCount = 0;
		try {
			ResultSet rs = db.getConnection().createStatement()
					.executeQuery("SELECT pegs_count FROM games WHERE game_id = " + gameId);
			rs.next();
			pegCount = rs.getInt(1);
		} catch (Exception ex) {
		}
		return pegCount;
	}

	private String generateAnswer(int pegCount) {
		Random rand = new Random();
		int randInt;
		boolean hasColor = false;
		String list = "";
		for (int i = 0; i < pegCount; i++) {
			randInt = rand.nextInt(8);
			hasColor = false;
			switch (randInt) {
			case 0:
				if (!list.contains("RED")) {
					list += "RED";
				} else {
					i--;
					hasColor = true;
				}
				break;
			case 1:
				if (!list.contains("GREEN")) {
					list += "GREEN";
				} else {
					i--;
					hasColor = true;
				}
				break;
			case 2:
				if (!list.contains("BLUE")) {
					list += "BLUE";
				} else {
					i--;
					hasColor = true;
				}
				break;
			case 3:
				if (!list.contains("YELLOW")) {
					list += "YELLOW";
				} else {
					i--;
					hasColor = true;
				}
				break;
			case 4:
				if (!list.contains("BROWN")) {
					list += "BROWN";
				} else {
					i--;
					hasColor = true;
				}
				break;
			case 5:
				if (!list.contains("ORANGE")) {
					list += "ORANGE";
				} else {
					i--;
					hasColor = true;
				}
				break;
			case 6:
				if (!list.contains("BLACK")) {
					list += "BLACK";
				} else {
					i--;
					hasColor = true;
				}
				break;
			case 7:
				if (!list.contains("WHITE")) {
					list += "WHITE";
				} else {
					i--;
					hasColor = true;
				}
				break;
			}
			if (i != pegCount - 1 && hasColor == false) {
				list += ",";
			}
		}
		return list;
	}

	private boolean isInventoryEmpty(Inventory inv) {
		return inv.firstEmpty() == 0;
	}

	private Location getLocation(int gameId) {
		String x, y, z, worldString;
		String coords;
		String[] splitCoords;
		World world;
		Location location = null;
		try {
			ResultSet rs = db.getConnection().createStatement().executeQuery("SELECT world FROM games WHERE game_id = " + gameId);
			rs.next();
			worldString = rs.getString(1);
			rs = db.getConnection().createStatement().executeQuery("SELECT main_room_teleport FROM games WHERE game_id = " + gameId);
			rs.next();
			coords = rs.getString(1);
			splitCoords = coords.split("\\,");
			x = splitCoords[0];
			y = splitCoords[1];
			z = splitCoords[2];
			world = Bukkit.getServer().getWorld(worldString);
			location = new Location(world, Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
		} catch (Exception ex) {
		}
		return location;
	}

	private boolean gameExists(int gameId) {
		boolean exists = false;
		try {
			ResultSet rs = db.getConnection().createStatement().executeQuery("SELECT game_id FROM games WHERE game_id = " + gameId);
			if (rs.next())
				exists = true;
		} catch (Exception ex) {
		}
		return exists;
	}

	private boolean isInt(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (Exception ex) {
		}
		return false;
	}

	private void setOccupied(int gameId) {
		try {
			db.getConnection().createStatement().executeUpdate("UPDATE games SET is_played = 1 WHERE game_id = " + gameId);
		} catch (Exception ex) {
		}
	}

}
