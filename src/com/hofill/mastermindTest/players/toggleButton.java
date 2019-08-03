package com.hofill.mastermindTest.players;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.hofill.mastermindTest.main;
import com.hofill.mastermindTest.mysql.MySQL;

import net.md_5.bungee.api.ChatColor;

public class toggleButton implements Listener {

	MySQL db = new MySQL();

	@EventHandler
	public void onToggleButton(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Material mat = event.getClickedBlock().getType();
			if (Tag.BUTTONS.isTagged(mat) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Player player = event.getPlayer();
				int gameId = getPlaying(player.getName());
				if (gameId != -1) {
					int guesses = getGuesses(gameId);
					int pegCount = getLength("pegs_count", gameId);
					int gameLength = getLength("game_length", gameId);
					// Get coords of buttons
					String buttonCoord = getCoords("button_coord", gameId);
					String[] buttons = buttonCoord.split("\\|");
					Location buttonLocation = event.getClickedBlock().getLocation();
					boolean isRegisteredButton = false;
					for (String coordButtonTemp : buttons) {
						String[] xyz = coordButtonTemp.split(",");
						if (Integer.parseInt(xyz[0]) == buttonLocation.getBlockX()
								&& Integer.parseInt(xyz[1]) == buttonLocation.getBlockY()
								&& Integer.parseInt(xyz[2]) == buttonLocation.getBlockZ()) {
							isRegisteredButton = true;
							break;
						}
					}
					if(isRegisteredButton) {
						// Get coords of the guess placement blocks
						String coordGuess = getCoords("coord_main_guess", gameId);
						String[] coordGuessSplit = coordGuess.split("\\|");
						ArrayList<String> blocksGuessed = new ArrayList<String>();
						World world = Bukkit.getServer().getWorld(getCoords("world", gameId));
						for (String coord : coordGuessSplit) {
							String[] splitCoordTemp = coord.split("\\,");
							Block block = world.getBlockAt(Integer.parseInt(splitCoordTemp[0]),
									Integer.parseInt(splitCoordTemp[1]), Integer.parseInt(splitCoordTemp[2]));
							if (block.getType().name() != "AIR" && Tag.WOOL.isTagged(block.getType())) {
								blocksGuessed.add(block.getType().name());
							}
						}
						if (blocksGuessed.size() == 4) {
							int right = 0;
							int wrong = 0;
							String answer = getStringProgress("answer", gameId);
							String[] answerBlocks = answer.split("\\,");
							int i, j;
							for (i = 0; i < pegCount; i++) {
								if (blocksGuessed.get(i).equals(answerBlocks[i] + "_WOOL")) {
									right++;
									continue;
								} else {
									for (j = 0; j < pegCount; j++) {
										if (i == j)
											continue;
										if (blocksGuessed.get(j).equals(answerBlocks[i] + "_WOOL")) {
											wrong++;
											break;
										}
									}
								}
							}
							// Get materials of right and wrong pegs
							String pegRight = getCoords("peg_correct_material", gameId);
							String pegWrong = getCoords("peg_wrong_material", gameId);
							// Get coords of the play zone and pegs zone
							String coordPegs = getCoords("coord_pegs", gameId);
							String[] coordPegsSplit = coordPegs.split("\\|");
							String coordPlayZone = getCoords("guess_coord", gameId);
							String[] coordPlayZoneSplit = coordPlayZone.split("\\|");
							boolean hasWon = false;
							if (right == pegCount) {
								hasWon = true;
							}
							int k = 0;
							for (i = guesses * pegCount; i < ((guesses * pegCount) + pegCount); i++) {
								String[] splitTempPlayZone = coordPlayZoneSplit[i].split("\\,");
								String[] splitTempPegCoord = coordPegsSplit[i].split("\\,");
								world.getBlockAt(Integer.parseInt(splitTempPlayZone[0]), Integer.parseInt(splitTempPlayZone[1]),
										Integer.parseInt(splitTempPlayZone[2]))
										.setType(Material.getMaterial(blocksGuessed.get(k)));
								if (right > 0) {
									world.getBlockAt(Integer.parseInt(splitTempPegCoord[0]),
											Integer.parseInt(splitTempPegCoord[1]), Integer.parseInt(splitTempPegCoord[2]))
											.setType(Material.getMaterial(pegRight));
									right--;
								} else if (wrong > 0) {
									world.getBlockAt(Integer.parseInt(splitTempPegCoord[0]),
											Integer.parseInt(splitTempPegCoord[1]), Integer.parseInt(splitTempPegCoord[2]))
											.setType(Material.getMaterial(pegWrong));
									wrong--;
								}
								k++;
							}
							for (String coord : coordGuessSplit) {
								String[] coordGuessTemp = coord.split("\\,");
								Location location = new Location(world, Integer.parseInt(coordGuessTemp[0]),
										Integer.parseInt(coordGuessTemp[1]), Integer.parseInt(coordGuessTemp[2]));
								world.getBlockAt(location).setType(Material.AIR);
							}
							k = 0;
							Plugin plugin = main.getPlugin(main.class);
							if (hasWon) {
								String winDisplay = getCoords("win_display_coord", gameId);
								String[] splitWinDisplayCoord = winDisplay.split("\\|");
								ArrayList<Location> locFireworks = new ArrayList<Location>();
								for (String coord : splitWinDisplayCoord) {
									String[] coordTemp = coord.split("\\,");
									Location location = new Location(world, Integer.parseInt(coordTemp[0]),
											Integer.parseInt(coordTemp[1]), Integer.parseInt(coordTemp[2]));
									world.getBlockAt(location).setType(Material.getMaterial(answerBlocks[k] + "_WOOL"));
									locFireworks.add(location);
									k++;
								}
								player.sendMessage(ChatColor.GREEN + "You won! The correct sequence was: " + answer);
								// Fireworks
								new BukkitRunnable() {
									int count = 0;
		
									@Override
									public void run() {
										if (count + 1 <= locFireworks.size()) {
											Firework fw = (Firework) locFireworks.get(count).getWorld()
													.spawnEntity(locFireworks.get(count), EntityType.FIREWORK);
											FireworkMeta fwm = fw.getFireworkMeta();
											Color[] colors = { Color.RED, Color.BLUE, Color.GREEN };
											fwm.addEffect(FireworkEffect.builder().withColor(colors).flicker(true).build());
											fwm.setPower(0);
											fw.setFireworkMeta(fwm);
											new BukkitRunnable() {
												@Override
												public void run() {
													fw.detonate();
												}
											}.runTaskLater(plugin, 1);
											count++;
										} else {
											this.cancel();
										}
									}
								}.runTaskTimer(plugin, 0, 5);
								player.getInventory().clear();
								// Wait before teleporting
								new BukkitRunnable() {
									@Override
									public void run() {
										String locTeleport = getStringProgress("teleport", gameId);
										String[] locTeleportCoords = locTeleport.split("\\,");
										stopPlaying(gameId);
										Location toTeleport = new Location(world, Integer.parseInt(locTeleportCoords[0]),
												Integer.parseInt(locTeleportCoords[1]), Integer.parseInt(locTeleportCoords[2]));
										player.teleport(toTeleport);
										resetGame(gameId);
									}
								}.runTaskLater(plugin, 60);
							} else if (guesses + 1 == gameLength) {
								player.sendMessage(ChatColor.BLUE + "You lost! The correct sequence was: " + answer);
								player.getInventory().clear();
								new BukkitRunnable() {
									@Override
									public void run() {
										String locTeleport = getStringProgress("teleport", gameId);
										String[] locTeleportCoords = locTeleport.split("\\,");
										stopPlaying(gameId);
										Location toTeleport = new Location(world, Double.parseDouble(locTeleportCoords[0]),
												Double.parseDouble(locTeleportCoords[1]),
												Double.parseDouble(locTeleportCoords[2]));
										player.teleport(toTeleport);
										resetGame(gameId);
									}
								}.runTaskLater(plugin, 60);
							}
							updateGuessNumber(guesses + 1, gameId);
						} else {
							player.sendMessage(ChatColor.RED + "You must fill up the guess space and use wool!");
						}
					}
				}
			}
		}
	}

	private int getPlaying(String player) {
		int gameId = -1;
		try {
			ResultSet rs = db.getConnection().createStatement()
					.executeQuery("SELECT game_id FROM games_in_progress WHERE player = '" + player + "'");
			if (rs.next()) {
				gameId = rs.getInt(1);
			}
		} catch (Exception ex) {
		}
		return gameId;
	}

	private String getCoords(String column, int gameId) {
		String coords = "";
		try {
			ResultSet rs = db.getConnection().createStatement()
					.executeQuery("SELECT " + column + " FROM games WHERE game_id = " + gameId);
			if (rs.next()) {
				coords = rs.getString(1);
			}
		} catch (Exception ex) {
		}
		return coords;
	}

	private int getLength(String column, int gameId) {
		int length = -1;
		try {
			ResultSet rs = db.getConnection().createStatement()
					.executeQuery("SELECT " + column + " FROM games WHERE game_id = " + gameId);
			rs.next();
			length = rs.getInt(1);
		} catch (Exception ex) {
		}
		return length;
	}
	
	private void resetGame(int gameId) {
		String unplayedMaterialString = getCoords("unplayed_material", gameId);
		String beforeSolutionMaterialString = getCoords("before_solution_material", gameId);
		String worldString = getCoords("world", gameId);
		String winDisplayCoord = getCoords("win_display_coord", gameId);
		String coordPegs = getCoords("coord_pegs", gameId);
		String coordMainGuess = getCoords("coord_main_guess", gameId);
		String guessCoord = getCoords("guess_coord", gameId);
		Material unplayedMaterial = Material.getMaterial(unplayedMaterialString);
		Material beforeSolutionMaterial = Material.getMaterial(beforeSolutionMaterialString);
		World world = Bukkit.getServer().getWorld(worldString);
		String[] winDisplayCoordSplit = winDisplayCoord.split("\\|");
		String[] coordPegsSplit = coordPegs.split("\\|");
		String[] coordMainGuessSplit = coordMainGuess.split("\\|");
		String[] guessCoordSplit = guessCoord.split("\\|");
		int x,y,z;
		for(String winDisplayTemp : winDisplayCoordSplit) {
			String[] xyz = winDisplayTemp.split("\\,");
			x = Integer.parseInt(xyz[0]);
			y = Integer.parseInt(xyz[1]);
			z = Integer.parseInt(xyz[2]);
			world.getBlockAt(x,y,z).setType(beforeSolutionMaterial);;
		}
		for(String coordPegsTemp : coordPegsSplit) {
			String[] xyz = coordPegsTemp.split("\\,");
			x = Integer.parseInt(xyz[0]);
			y = Integer.parseInt(xyz[1]);
			z = Integer.parseInt(xyz[2]);
			world.getBlockAt(x,y,z).setType(unplayedMaterial);;
		}
		for(String coordMainTemp : coordMainGuessSplit) {
			String[] xyz = coordMainTemp.split("\\,");
			x = Integer.parseInt(xyz[0]);
			y = Integer.parseInt(xyz[1]);
			z = Integer.parseInt(xyz[2]);
			world.getBlockAt(x,y,z).setType(Material.AIR);
		}
		for(String guessCoordTemp : guessCoordSplit) {
			String[] xyz = guessCoordTemp.split("\\,");
			x = Integer.parseInt(xyz[0]);
			y = Integer.parseInt(xyz[1]);
			z = Integer.parseInt(xyz[2]);
			world.getBlockAt(x,y,z).setType(Material.AIR);;
		}
	}

	private int getGuesses(int gameId) {
		int guesses = -1;
		try {
			ResultSet rs = db.getConnection().createStatement()
					.executeQuery("SELECT at_guess from games_in_progress WHERE game_id = " + gameId);
			rs.next();
			guesses = rs.getInt(1);
		} catch (Exception ex) {
		}
		return guesses;
	}

	private String getStringProgress(String column, int gameId) {
		String answer = "";
		try {
			ResultSet rs = db.getConnection().createStatement()
					.executeQuery("SELECT " + column + " FROM games_in_progress WHERE game_id = " + gameId);
			if (rs.next()) {
				answer = rs.getString(1);
			}
		} catch (Exception ex) {
		}
		return answer;
	}

	private void stopPlaying(int gameId) {
		try {
			db.getConnection().createStatement().executeUpdate("DELETE FROM games_in_progress WHERE game_id = " + gameId);
			db.getConnection().createStatement().executeUpdate("UPDATE games SET is_played = false WHERE game_id = " + gameId);
		} catch (Exception ex) {
		}
	}

	private void updateGuessNumber(int value, int gameId) {
		try {
			db.getConnection().createStatement()
					.executeUpdate("UPDATE games_in_progress SET at_guess = " + value + " WHERE game_id = " + gameId);
		} catch (Exception ex) {
		}
	}

}
