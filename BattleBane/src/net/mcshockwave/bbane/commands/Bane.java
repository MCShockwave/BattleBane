package net.mcshockwave.bbane.commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.bbane.BattleBane;
import net.mcshockwave.bbane.teams.BBTeam;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.apache.commons.lang.WordUtils;

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
				
				if (c.equalsIgnoreCase("resetWorld")) {
					BattleBane.reset();
				}
				
				if (c.equalsIgnoreCase("genStructures")) {
					BattleBane.genStructures();
				}
				
				if (c.equalsIgnoreCase("add")) {
					Player pl = args.length > 2 ? Bukkit.getPlayer(args[2]) : p;
					BBTeam te = BBTeam.valueOf(WordUtils.capitalizeFully(args[1]));
					
					te.addPlayer(pl);
				}
				
				if (c.equalsIgnoreCase("remove")) {
					Player pl = args.length > 1 ? Bukkit.getPlayer(args[1]) : p;
					
					BBTeam.removePlayer(pl);
				}
				
				if (c.equalsIgnoreCase("togstarted")) {
					BattleBane.started = !BattleBane.started;
					p.sendMessage("§aStarted is now " + BattleBane.started);
				}

			}
		}

		return false;
	}

}
