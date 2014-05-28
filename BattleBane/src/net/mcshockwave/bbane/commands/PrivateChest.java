package net.mcshockwave.bbane.commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.bbane.BattleBane;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class PrivateChest implements CommandExecutor {

	public static HashMap<String, Inventory>	chests	= new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.getWorld() != BattleBane.wor()) {
				p.sendMessage("§cYou cannot open that here!");
				return false;
			}

			if (!chests.containsKey(p.getName())) {
				boolean large = SQLTable.hasRank(p.getName(), Rank.OBSIDIAN);
				int slots = large ? 18 : 9;

				Inventory ch = Bukkit.createInventory(p, slots, "Private Storage - " + slots + " slots");
				chests.put(p.getName(), ch);
			}
			p.openInventory(chests.get(p.getName()));
		}

		return false;
	}

}
