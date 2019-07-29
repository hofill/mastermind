package com.hofill.mastermindTest.inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class InventoryGuessNumber {

	public static Inventory inventory = Bukkit.createInventory(null, 9, "Select number of guesses");

	static{
		inventory.setItem(2, getWoolGuesses("12 Guesses", Material.GREEN_WOOL, ChatColor.GREEN));
		inventory.setItem(4, getWoolGuesses("10 Guesses", Material.ORANGE_WOOL, ChatColor.GOLD));
		inventory.setItem(6, getWoolGuesses("8 Guesses", Material.RED_WOOL, ChatColor.RED));
	}

	private static ItemStack getWoolGuesses(String guesses, Material material, ChatColor color) {
		ItemStack guessItem = new ItemStack(material);
		ItemMeta guessItemMeta = guessItem.getItemMeta();
		guessItemMeta.setDisplayName(color + guesses);
		guessItem.setItemMeta(guessItemMeta);
		return guessItem;
	}

}
