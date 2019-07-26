package com.hofill.mastermindTest.players;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.hofill.mastermindTest.mysql.MySQL;

public class selectUnplayedBlock implements Listener {

	MySQL db = new MySQL();
	ArrayList<String> creationState = new ArrayList<String>();
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		creationState.clear();
		creationState = getState("creation_state");
		if (creationState.contains(player.getName())) {
			Item mat = event.getItemDrop();
			player.getInventory().addItem(mat.getItemStack());
		}
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
