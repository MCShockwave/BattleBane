package net.mcshockwave.bbane.commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.bbane.BBKit;
import net.mcshockwave.bbane.BattleBane;
import net.mcshockwave.bbane.teams.BBTeam;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

import java.util.Arrays;

import org.apache.commons.lang.WordUtils;

public class Bane implements CommandExecutor {

	@SuppressWarnings("deprecation")
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
					String[] worlds = { "BattleBaneLobby", "BattleBaneArena", "BattleBaneArenaBuild", "BattleBaneWorld" };
					if (!Arrays.asList(worlds).contains(args[1])) {
						p.sendMessage("§cInvalid World: " + args[1]);
						return false;
					}
					World w = Bukkit.getWorld(args[1]);
					if (w != null) {
						p.teleport(w.getSpawnLocation());
					} else {
						p.sendMessage("§cCould not find world: Loading \"" + args[1] + "\"");
						new WorldCreator(args[1]).type(
								args[1].equalsIgnoreCase("BattleBaneWorld") ? WorldType.NORMAL : WorldType.FLAT)
								.createWorld();
					}
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

				if (c.equalsIgnoreCase("togauto")) {
					BattleBane.autoArena = !BattleBane.autoArena;
					p.sendMessage("§aAuto Arena is now " + BattleBane.autoArena);
				}

				if (c.equalsIgnoreCase("giveselectors")) {
					for (Player p2 : Bukkit.getOnlinePlayers()) {
						if (BBTeam.getTeamFor(p2) == null) {
							BBKit.giveSelectors(p2);
						}
					}
				}

				if (c.equalsIgnoreCase("timearena")) {
					BattleBane.startArenaCount(Integer.parseInt(args[1]));
				}

				if (c.equalsIgnoreCase("arenastopcount")) {
					BattleBane.stopArenaCount();
				}

				if (c.equalsIgnoreCase("startarena")) {
					BattleBane.startArena();
				}

				if (c.equalsIgnoreCase("endarena")) {
					BattleBane.endArena(null);
				}

				if (c.equalsIgnoreCase("setneeded")) {
					BattleBane.pointsNeeded = Integer.parseInt(args[1]);
					Score max = BattleBane.score.getObjective("Points").getScore(
							Bukkit.getOfflinePlayer("§7 Points Needed"));
					max.setScore(BattleBane.pointsNeeded);
				}

				if (c.equalsIgnoreCase("save")) {
					Bukkit.broadcastMessage("Saving build world...");
					for (Player p2 : Bukkit.getOnlinePlayers()) {
						if (p2.getWorld() == BattleBane.areBuild()) {
							p2.teleport(BattleBane.lob().getSpawnLocation());
						}
					}

					BattleBane.areBuild().save();

					Bukkit.unloadWorld("BattleBaneArenaBuild", true);
					BattleBane.deleteWorld("BattleBaneArenaBackup");
					BattleBane.copyWorld("BattleBaneArenaBuild", "BattleBaneArenaBackup");
					new WorldCreator("BattleBaneArenaBuild").createWorld();
				}

				if (c.equalsIgnoreCase("reloadArena")) {
					BattleBane.resetArena(false);
				}

				if (c.equalsIgnoreCase("restart")) {
					restart();
				}

				if (c.equalsIgnoreCase("center")) {
					BattleBane.generateCenter();
				}
			}
		}

		return false;
	}

	public static void restart() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.kickPlayer("§e§lServer Restarting");
		}
		BattleBane.deleteWorld(BattleBane.wor());
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
	}
}
