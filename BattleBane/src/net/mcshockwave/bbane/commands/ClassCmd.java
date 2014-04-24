package net.mcshockwave.bbane.commands;

import net.mcshockwave.bbane.BBKit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			
			BBKit.getClassMenu(p).open(p);
		}
		return false;
	}

}
