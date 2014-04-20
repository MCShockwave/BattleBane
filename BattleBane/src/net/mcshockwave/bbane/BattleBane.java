package net.mcshockwave.bbane;

import net.mcshockwave.bbane.commands.Bane;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BattleBane extends JavaPlugin {

	public static BattleBane	ins;

	public void onEnable() {
		ins = this;
		Bukkit.getPluginManager().registerEvents(new DefaultListener(), this);

		getCommand("bane").setExecutor(new Bane());

		Bukkit.createWorld(new WorldCreator("BattleBaneLobby").type(WorldType.FLAT));
		Bukkit.createWorld(new WorldCreator("BattleBaneArena").type(WorldType.FLAT));
		Bukkit.createWorld(new WorldCreator("BattleBaneWorld").type(WorldType.NORMAL));
	}

	public static void reset() {
		Bukkit.broadcastMessage("§cRESETTING WORLD");
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.teleport(lob().getSpawnLocation());
		}
		
		Bukkit.unloadWorld(wor(), false);
		File wfile = new File("BattleBaneWorld/");
		wfile.delete();
	}

	public static World lob() {
		return Bukkit.getWorld("BattleBaneLobby");
	}

	public static World are() {
		return Bukkit.getWorld("BattleBaneArena");
	}

	public static World wor() {
		return Bukkit.getWorld("BattleBaneWorld");
	}

}
