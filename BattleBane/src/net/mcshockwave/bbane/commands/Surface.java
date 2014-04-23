package net.mcshockwave.bbane.commands;

import net.mcshockwave.bbane.BattleBane;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Surface implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.getWorld() == BattleBane.wor()) {
				Location loc = p.getEyeLocation().clone();
				loc.setY(loc.getWorld().getHighestBlockYAt(loc));
				p.teleport(loc);
				p.sendMessage("§aTeleported");
			}
		}
		return false;
	}

}
