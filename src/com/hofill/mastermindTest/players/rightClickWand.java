package com.hofill.mastermindTest.players;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
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
	ArrayList<String> coordWinDisplayState = new ArrayList<String>(); // Number of pegs
	ArrayList<String> pegsCoordState = new ArrayList<String>(); // Coordinates of pegs
	ArrayList<String> guessCoordState = new ArrayList<String>(); // Coordinates of guesses
	ArrayList<String> pegsMainCoordsState = new ArrayList<String>(); // Coordinates of main pegs
	ArrayList<String> buttonCoordState = new ArrayList<String>(); // Coordinates of buttons
	ArrayList<String> mainRoomState = new ArrayList<String>(); // Coordinates of main room

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
		if (eventItem != null)
			createGame.setAmount(eventItem.getAmount());
		if (eventItem != null && eventItem.equals(createGame)) {
			// Clear arrays
			gameLengthState.clear();
			pegsCountState.clear();
			coordWinDisplayState.clear();
			pegsCoordState.clear();
			pegsMainCoordsState.clear();
			buttonCoordState.clear();
			mainRoomState.clear();
			guessCoordState.clear();
			// Get states from database
			gameLengthState = getState("game_length_state");
			pegsCountState = getState("pegs_count_state");
			coordWinDisplayState = getState("coord_win_display_state");
			pegsCoordState = getState("pegs_coord_state");
			pegsMainCoordsState = getState("pegs_main_guess_state");
			guessCoordState = getState("guess_coord_state");
			buttonCoordState = getState("button_coord_state");
			mainRoomState = getState("main_room_state");
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
				if (event.getAction() == Action.RIGHT_CLICK_AIR) {
					if (gameLengthState.contains(player.getName())) {
						player.openInventory(InventoryGuessNumber.inventory);
					} else if (pegsCountState.contains(player.getName())) {
						player.openInventory(InventoryPegsCount.inventory);
					} else {
						player.sendMessage(ChatColor.RED + "You must right click on a block!");
					}
				} else {
					if (coordWinDisplayState.contains(player.getName())) {
						Location location = event.getClickedBlock().getLocation();
						String coord = getCoord("win_display_coord", player.getName());
						String newCoord = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
								+ "|";
						if (!coord.contains(newCoord)) {
							int count = StringUtils.countMatches(coord, "|");
							int pegsCount = getGameSizes("pegs_count", player.getName());
							coord += (location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
									+ "|");
							updateCoord("win_display_coord", player.getName(), coord);
							player.sendMessage(ChatColor.BLUE + "Block selected.");
							if ((count + 1) % pegsCount == 0) {
								player.sendMessage(ChatColor.BLUE + "Win display set.");
								player.sendMessage(ChatColor.BLUE
										+ "Right click with the wand on the first row of pegs. (In order)");
								updateCoord("world", player.getName(), player.getWorld().getName());
								updateState("coord_win_display_state", "pegs_coord_state", player.getName());
								String[] winDisplay = coord.split("\\|");
								for (String roughCoords : winDisplay) {
									String[] coords = roughCoords.split("\\,");
									Material mat = Material
											.getMaterial(getCoord("before_solution_material", player.getName()));
									World world = player.getWorld();
									world.getBlockAt(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
											Integer.parseInt(coords[2])).setType(mat);
								}
							}
						} else {
							player.sendMessage(ChatColor.RED + "You can't select the same block multiple times!");
						}
					} else if (pegsCoordState.contains(player.getName())) {
						Location location = event.getClickedBlock().getLocation();
						String coord = getCoord("coord_pegs", player.getName());
						String newCoord = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
								+ "|";
						if (!coord.contains(newCoord)) {
							int gameSize = getGameSizes("game_length", player.getName());
							int count = StringUtils.countMatches(coord, "|");
							int pegsCount = getGameSizes("pegs_count", player.getName());
							coord += newCoord;
							updateCoord("coord_pegs", player.getName(), coord);
							player.sendMessage(ChatColor.BLUE + "Block selected.");
							if ((count + 1) % pegsCount == 0) {
								player.sendMessage(ChatColor.BLUE + "Row " + (count + 1) / pegsCount + " complete.");
								player.sendMessage(
										ChatColor.BLUE + "" + (gameSize - ((count + 1) / pegsCount)) + " more to go.");
								String[] winDisplay = coord.split("\\|");
								for (String roughCoords : winDisplay) {
									String[] coords = roughCoords.split("\\,");
									Material mat = Material
											.getMaterial(getCoord("unplayed_material", player.getName()));
									World world = player.getWorld();
									world.getBlockAt(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
											Integer.parseInt(coords[2])).setType(mat);
								}
							}
							if (gameSize - ((count + 1) / pegsCount) == 0) {
								player.sendMessage(ChatColor.BLUE + "All rows complete!");
								player.sendMessage(ChatColor.BLUE
										+ "Right click with the wand on the blocks where you place your guess.");
								updateState("pegs_coord_state", "pegs_main_guess_state", player.getName());
							}
						} else {
							player.sendMessage(ChatColor.RED + "You can't select the same block multiple times!");
						}
					} else if (pegsMainCoordsState.contains(player.getName())) {
						Location location = event.getClickedBlock().getLocation();
						String coord = getCoord("coord_main_guess", player.getName());
						String newCoord = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
								+ "|";
						if (!coord.contains(newCoord)) {
							int count = StringUtils.countMatches(coord, "|");
							int pegsCount = getGameSizes("pegs_count", player.getName());
							coord += newCoord;
							updateCoord("coord_main_guess", player.getName(), coord);
							player.sendMessage(ChatColor.BLUE + "Block selected.");
							if ((count + 1) % pegsCount == 0) {
								String[] winDisplay = coord.split("\\|");
								player.sendMessage(ChatColor.BLUE + "Right click, in order, on the play zone blocks.");
								updateState("pegs_main_guess_state", "guess_coord_state", player.getName());
								for (String roughCoords : winDisplay) {
									String[] coords = roughCoords.split("\\,");
									Material mat = Material.getMaterial("AIR");
									World world = player.getWorld();
									world.getBlockAt(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
											Integer.parseInt(coords[2])).setType(mat);
								}
							}

						} else {
							player.sendMessage(ChatColor.RED + "You can't select the same block multiple times!");
						}
					} else if (guessCoordState.contains(player.getName())) {
						Location location = event.getClickedBlock().getLocation();
						String coord = getCoord("guess_coord", player.getName());
						String newCoord = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
								+ "|";
						if (!coord.contains(newCoord)) {
							int gameSize = getGameSizes("game_length", player.getName());
							int count = StringUtils.countMatches(coord, "|");
							int pegsCount = getGameSizes("pegs_count", player.getName());
							coord += newCoord;
							updateCoord("guess_coord", player.getName(), coord);
							player.sendMessage(ChatColor.BLUE + "Block selected.");
							if ((count + 1) % pegsCount == 0) {
								player.sendMessage(ChatColor.BLUE + "Row " + (count + 1) / pegsCount + " complete.");
								player.sendMessage(
										ChatColor.BLUE + "" + (gameSize - ((count + 1) / pegsCount)) + " more to go.");
								String[] winDisplay = coord.split("\\|");
								for (String roughCoords : winDisplay) {
									String[] coords = roughCoords.split("\\,");
									Material mat = Material.getMaterial("AIR");
									World world = player.getWorld();
									world.getBlockAt(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
											Integer.parseInt(coords[2])).setType(mat);
								}
							}
							if (gameSize - ((count + 1) / pegsCount) == 0) {
								player.sendMessage(ChatColor.BLUE + "All rows complete!");
								player.sendMessage(ChatColor.BLUE
										+ "Type in chat the number of buttons you want to have to confirm guess (1-4).");
								updateState("guess_coord_state", "button_amount_state", player.getName());
							}
						} else {
							player.sendMessage(ChatColor.RED + "You can't select the same block multiple times!");
						}
					} else if (buttonCoordState.contains(player.getName())) {
						Location location = event.getClickedBlock().getLocation();
						String coord = getCoord("button_coord", player.getName());
						String newCoord = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
								+ "|";
						if (!coord.contains(newCoord)) {
							if (Tag.BUTTONS.isTagged(event.getClickedBlock().getType())) {
								int count = StringUtils.countMatches(coord, "|");
								int buttonCount = getGameSizes("button_amount", player.getName());
								coord += newCoord;
								updateCoord("button_coord", player.getName(), coord);
								player.sendMessage(ChatColor.BLUE + "Block selected.");
								if ((count + 1) % buttonCount == 0) {
									player.sendMessage(ChatColor.BLUE
											+ "Right click with the wand on the teleport block of the guess room.");
									updateState("button_coord_state", "main_room_state", player.getName());
								}
							} else {
								player.sendMessage(ChatColor.RED + "You can only select buttons!");
							}
						} else {
							player.sendMessage(ChatColor.RED + "You can't select the same button multiple times!");
						}
					} else if (mainRoomState.contains(player.getName())) {
						Location location = event.getClickedBlock().getLocation();
						int y = location.getBlockY() + 1;
						String newCoord = location.getBlockX() + "," + y + "," + location.getBlockZ();
						updateCoord("main_room_teleport", player.getName(), newCoord);
						player.sendMessage(ChatColor.BLUE + "Block selected.");
						player.sendMessage(ChatColor.GREEN + "Game with the number " + getGameId(player.getName())
								+ " generated successfully.");
						player.sendMessage(ChatColor.GREEN + "Use /mastermind play <gameId> to play.");
						player.sendMessage(ChatColor.GREEN + "To edit the game, use /mastermind edit <state>.");
						removeState(player.getName());
					}
				}
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

	private void updateCoord(String column, String player, String coord) {
		int gameId = getGameId(player);
		try {
			Connection conn = db.openConnection();
			conn.createStatement()
					.executeUpdate("UPDATE games SET " + column + " = '" + coord + "' WHERE game_id = " + gameId);
			conn.close();
		} catch (Exception ex) {
		}
	}

	private String getCoord(String column, String player) {
		String coordString = "";
		int gameId = getGameId(player);
		try {
			Connection conn = db.openConnection();
			ResultSet rs = conn.createStatement()
					.executeQuery("SELECT " + column + " FROM games WHERE game_id = '" + gameId + "'");
			rs.next();
			coordString = rs.getString(1);
			if (coordString == null)
				coordString = "";
			conn.close();
		} catch (Exception ex) {
		}
		return coordString;
	}

	private int getGameSizes(String column, String player) {
		int size = 0;
		int gameId = getGameId(player);
		try {
			Connection conn = db.openConnection();
			ResultSet rs = conn.createStatement()
					.executeQuery("SELECT " + column + " FROM games WHERE game_id = '" + gameId + "'");
			rs.next();
			size = rs.getInt(1);
			conn.close();
		} catch (Exception ex) {
		}
		return size;
	}

	private void removeState(String player) {
		try {
			Connection conn = db.openConnection();
			conn.createStatement().executeUpdate("DELETE FROM current_state WHERE player = '" + player + "'");
			conn.close();
		} catch (Exception ex) {
		}
	}

	private int getGameId(String player) {
		int gameId = 0;
		try {
			Connection conn = db.openConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT game_id FROM current_state WHERE player = ?");
			ps.setString(1, player);
			ResultSet rs = ps.executeQuery();
			rs.next();
			gameId = rs.getInt(1);
			conn.close();
		} catch (Exception ex) {
		}
		return gameId;
	}

	private void updateState(String initialState, String changedState, String player) {
		try {
			Connection conn = db.openConnection();
			PreparedStatement ps = conn
					.prepareStatement("UPDATE current_state SET state = ? WHERE state = ? AND player = ?");
			ps.setString(1, changedState);
			ps.setString(2, initialState);
			ps.setString(3, player);
			ps.executeUpdate();
			conn.close();
		} catch (Exception ex) {
		}
	}

}
