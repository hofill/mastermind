package com.hofill.mastermind.inventories;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class InventoryPegsCount {

	public static Inventory inventory = Bukkit.createInventory(null, 9, "Colors per guess");

	static{
		inventory.setItem(2, getWoolGuesses("Length 4", Material.GREEN_WOOL, ChatColor.GREEN, Arrays.asList("4 colors per guess", "*DEFAULT*")));
		inventory.setItem(4, getWoolGuesses("Length 6", Material.ORANGE_WOOL, ChatColor.GOLD, Arrays.asList("6 colors per guess")));
		inventory.setItem(6, getWoolGuesses("Length 8", Material.RED_WOOL, ChatColor.RED, Arrays.asList("8 colors per guess")));
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
