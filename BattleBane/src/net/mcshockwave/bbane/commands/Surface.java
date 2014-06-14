package net.mcshockwave.bbane.commands;

import net.mcshockwave.bbane.BattleBane;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class Surface implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.getWorld() == BattleBane.wor()) {
				if (isSafe(p, 10)) {
					Location loc = p.getEyeLocation().clone();
					loc.setY(loc.getWorld().getHighestBlockYAt(loc));
					p.teleport(loc);
					p.sendMessage("§aTeleported");
				} else {
					p.sendMessage("§cYou can't do that while monsters are nearby!");
				}
			}
		}
		return false;
	}

	public boolean isSafe(Player p, int r) {
		for (Entity e : p.getNearbyEntities(r, r, r)) {
			if (e instanceof Monster) {
				return false;
			}
		}
		return true;
	}

}
