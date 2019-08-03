package com.hofill.mastermindTest;


import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.hofill.mastermindTest.autocomplete.TabAutocomplete;
import com.hofill.mastermindTest.commands.Mastermind;
import com.hofill.mastermindTest.commands.RemoveFromState;
import com.hofill.mastermindTest.commands.Setup;
import com.hofill.mastermindTest.mysql.DatabaseValues;
import com.hofill.mastermindTest.players.clickInInventory;
import com.hofill.mastermindTest.players.getChatNumber;
import com.hofill.mastermindTest.players.rightClickWand;
import com.hofill.mastermindTest.players.selectUnplayedBlock;
import com.hofill.mastermindTest.players.toggleButton;

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
