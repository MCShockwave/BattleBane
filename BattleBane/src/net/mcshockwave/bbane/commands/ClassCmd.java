package net.mcshockwave.bbane.commands;

import net.mcshockwave.bbane.BBKit;
import net.mcshockwave.bbane.DefaultListener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (!DefaultListener.canBuildBase(p.getLocation().getBlock())) {
				BBKit.getClassMenu(p, true).open(p);
			}
		}
		return false;
	}

}
