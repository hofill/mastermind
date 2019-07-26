package com.hofill.mastermindTest;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.hofill.mastermindTest.mysql.DatabaseValues;

public class ConfigManager {

	private main plugin = main.getPlugin(main.class);
	public FileConfiguration cfg;
	public File dbFile;

	public void setup() {
		dbFile = new File(plugin.getDataFolder(), "database.yml");

		if (!dbFile.exists()) {
			try {
				plugin.saveResource("database.yml", false);
			} catch (Exception ex) {
				main.tellConsole("Could not create database.yml");
			}
		}

		cfg = YamlConfiguration.loadConfiguration(dbFile);
		setValues();
		
	}

	public FileConfiguration getDBFile() {
		return cfg;
	}

	public void saveDBFile() {
		plugin.saveResource("database.yml", false);
		setValues();
	}
	
	public void reloadDBFile(){
		cfg = YamlConfiguration.loadConfiguration(dbFile);
	}

	public void setValues() {
		DatabaseValues.hostname = getDBFile().getString("hostname");
		DatabaseValues.username = getDBFile().getString("username");
		DatabaseValues.password = getDBFile().getString("password");
		DatabaseValues.port = getDBFile().getString("port");
		DatabaseValues.database = getDBFile().getString("database");
	}
	
}
