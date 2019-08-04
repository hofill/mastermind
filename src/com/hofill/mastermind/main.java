package com.hofill.mastermind;


import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.hofill.mastermind.mysql.DatabaseValues;
import com.hofill.mastermind.players.toggleButton;
import com.hofill.mastermind.autocomplete.TabAutocomplete;
import com.hofill.mastermind.commands.Mastermind;
import com.hofill.mastermind.commands.RemoveFromState;
import com.hofill.mastermind.commands.Setup;
import com.hofill.mastermind.players.clickInInventory;
import com.hofill.mastermind.players.getChatNumber;
import com.hofill.mastermind.players.rightClickWand;
import com.hofill.mastermind.players.selectUnplayedBlock;

import net.md_5.bungee.api.ChatColor;;

public class main extends JavaPlugin {

	private ConfigManager cfg;

	public void onEnable() {
		registerConfig(); // Getting config data
		registerConfigManager(); // Getting other data (database)
		registerCommands();
		registerEvents();
		registerAutoComplete();
	}

	public static void tellConsole(String msg) {
		Bukkit.getConsoleSender().sendMessage(msg);
	}

	public void registerCommands() {
		getCommand("setupgame").setExecutor(new Setup());
		getCommand("removefromstate").setExecutor(new RemoveFromState());
		getCommand("mastermind").setExecutor(new Mastermind());
	}
	
	public void registerAutoComplete() {
		getCommand("removefromstate").setTabCompleter(new TabAutocomplete());
		getCommand("mastermind").setTabCompleter(new TabAutocomplete());
	}
	
	public void registerEvents() {
		getServer().getPluginManager().registerEvents(new selectUnplayedBlock(), this);
		getServer().getPluginManager().registerEvents(new rightClickWand(), this);
		getServer().getPluginManager().registerEvents(new clickInInventory(), this);
		getServer().getPluginManager().registerEvents(new getChatNumber(), this);
		getServer().getPluginManager().registerEvents(new toggleButton(), this);
	}
	
	public void registerConfig() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
	}

	public void registerConfigManager() {
		cfg = new ConfigManager();
		cfg.setup();
		cfg.getDBFile().options().copyDefaults(true);
		if (DatabaseValues.hostname.isBlank() || DatabaseValues.username.isBlank() || DatabaseValues.port.isBlank()
				|| DatabaseValues.database.isBlank()) {
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "You can't leave blank spaces in your database.yml file (except for password)");
		}
	}

}
