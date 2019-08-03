package com.hofill.mastermindTest.autocomplete;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.hofill.mastermindTest.mysql.MySQL;

public class TabAutocomplete implements TabCompleter {

	MySQL db = new MySQL();

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

		if (cmd.getName().equalsIgnoreCase("removefromstate") && args.length == 1) {
			List<String> list = getList();
			return list;
		}
		else if(cmd.getName().equalsIgnoreCase("mastermind") && args.length == 1) {
			List<String> list = Arrays.asList("play","remove","edit","leave");
			return list;
		}

		return null;
	}

	private List<String> getList() {
		List<String> list = new ArrayList<String>();
		list.clear();
		try {
			Connection conn = db.openConnection();
			ResultSet rs = conn.createStatement().executeQuery("SELECT player FROM current_state");
			while (rs.next()) {
				list.add(rs.getString(1));
			}
		} catch (Exception ex) {
		}
		return list;

	}

}
