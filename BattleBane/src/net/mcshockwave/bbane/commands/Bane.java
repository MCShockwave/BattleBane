package net.mcshockwave.bbane.commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Bane implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (SQLTable.hasRank(p.getName(), Rank.JR_MOD)) {
				String c = args[0];

				if (c.equalsIgnoreCase("setspawn")) {
					Location l = p.getLocation();

					p.sendMessage(String.format("§cSpawn set to x%s y%s z%s in world " + p.getWorld().getName(),
							l.getBlockX(), l.getBlockY(), l.getBlockZ()));
					p.getWorld().setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
				}
				
				if (c.equalsIgnoreCase("gotoWorld")) {
					p.teleport(Bukkit.getWorld(args[1]).getSpawnLocation());
				}

			}
		}

		return false;
	}

}
