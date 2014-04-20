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
		genWorld();
	}

	public static void reset() {
		Bukkit.broadcastMessage("§c§nRESETTING WORLD, PREPARE FOR LAG");
		Bukkit.broadcastMessage(" ");
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.teleport(lob().getSpawnLocation());
		}

		if (Bukkit.unloadWorld(wor(), true)) {
			System.out.println("Unloaded world");
		} else {
			System.err.println("Couldn't unload world");
		}
		if (delete(new File("BattleBaneWorld"))) {
			System.out.println("Deleted world!");
			WorldCreator wc = new WorldCreator("worldname");
			wc.type(WorldType.NORMAL);
			wc.createWorld();
			Bukkit.reload();
		} else {
			System.err.println("Couldn't delete world");
		}

		genWorld();
		Bukkit.broadcastMessage("§aDONE");
	}

	public static boolean delete(File file) {
		if (file.isDirectory())
			for (File subfile : file.listFiles())
				if (!delete(subfile))
					return false;
		if (!file.delete())
			return false;
		return true;
	}

	public static void genWorld() {
		Bukkit.createWorld(new WorldCreator("BattleBaneWorld").type(WorldType.NORMAL));
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
