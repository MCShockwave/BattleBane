package net.mcshockwave.bbane.commands;

import net.mcshockwave.bbane.BattleBane;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildWorld implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player && sender.isOp()) {
			Player p = (Player) sender;
			
			p.teleport(BattleBane.areBuild().getSpawnLocation());
		}
		
		return false;
	}
}
