package net.mcshockwave.bbane;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.bbane.commands.Bane;
import net.mcshockwave.bbane.teams.BBTeam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleBane extends JavaPlugin {

	static Random				rand			= new Random();

	public static BattleBane	ins;

	public static Scoreboard	score;

	public static boolean		started			= true, arena = false;
	public static Arena			currentArena	= null;

	public void onEnable() {
		ins = this;
		Bukkit.getPluginManager().registerEvents(new DefaultListener(), this);

		score = Bukkit.getScoreboardManager().getMainScoreboard();

		getCommand("bane").setExecutor(new Bane());

		Bukkit.createWorld(new WorldCreator("BattleBaneLobby").type(WorldType.FLAT));
		Bukkit.createWorld(new WorldCreator("BattleBaneArena").type(WorldType.FLAT));
		Bukkit.createWorld(new WorldCreator("BattleBaneArenaBuild").type(WorldType.FLAT));
		genWorld();
	}

	public void onDisable() {
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
		} else {
			System.err.println("Couldn't delete world");
		}

		genWorld();
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
		boolean en = true;
		try {
			Plugin p = Bukkit.getPluginManager().getPlugin("TerrainControl");
			if (!p.isEnabled()) {
				Bukkit.getPluginManager().enablePlugin(p);
			}
		} catch (Exception e) {
			en = false;
			Bukkit.broadcastMessage("§cTerrainControl has thrown an error, defaulting to normal world generation");
		}

		WorldCreator c = new WorldCreator("BattleBaneWorld");
		if (en) {
			try {
				c.generator("TerrainControl");
			} catch (Exception e) {
				Bukkit.broadcastMessage("§cTerrainControl has thrown an error, defaulting to normal world generation");
			}
		}
		c.generateStructures(true);
		c.type(WorldType.NORMAL);
		c.environment(Environment.NORMAL);
		c.createWorld();

		wor().setSpawnLocation(0, wor().getHighestBlockYAt(0, 0) + 1, 0);

		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				genStructures();
			}
		}, 50l);
	}

	public static void genStructures() {
		Block cen = wor().getHighestBlockAt(0, 0);
		cen.getChunk().load();

		cen.setType(Material.SIGN_POST);

		try {
			Sign s = (Sign) cen.getState();
			s.setLine(1, "§8Center");
			s.update();
		} catch (Exception e) {
		}

		for (BBTeam t : BBTeam.values()) {
			Block b = wor().getHighestBlockAt(t.x, t.z);
			b.getChunk().load();

			b.setType(Material.SIGN_POST);

			t.spawn.setY(b.getLocation().getBlockY());

			try {
				Sign s = (Sign) b.getState();
				s.setLine(1, t.c + t.name());
				s.update();
			} catch (Exception e) {
			}
		}
	}

	public static void startArena() {
		arena = true;

		Arena ar = Arena.values()[rand.nextInt(Arena.values().length)];

		currentArena = ar;

		for (BBTeam bbt : BBTeam.values()) {
			List<Player> ready = getArenaReady(bbt);
			if (ready.size() > 0) {
				for (Player p : ready) {
					p.teleport(ar.spawns.get(bbt));
				}
			}
		}

		MCShockwave.broadcast(ChatColor.YELLOW, "Arena started on %s", ar.name);
	}

	public static void endArena(BBTeam winner) {
		arena = false;

		for (Entity e : are().getEntities()) {
			if (e instanceof Player) {
				Player p = (Player) e;

				if (BBTeam.getTeamFor(p) != null) {
					p.teleport(BBTeam.getTeamFor(p).spawn);
				}
			}
		}

		if (winner != null) {
			MCShockwave.broadcast(winner.c, "%s has won on arena %s", winner.name(), currentArena.name);
		} else {
			MCShockwave.broadcast("%s has won on arena %s", "Nobody", currentArena.name);
		}

		currentArena = null;
	}

	public static List<Player> getAllInArena(BBTeam te) {
		ArrayList<Player> pl = new ArrayList<>();

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (te.isTeam(p)) {
				if (p.getWorld() == are() && !p.isDead() && p.isValid()) {
					pl.add(p);
				}
			}
		}

		return pl;
	}

	public static List<Player> getArenaReady(BBTeam te) {
		ArrayList<Player> pl = new ArrayList<>();

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (te.isTeam(p)) {
				if (p.getWorld() == te.spawn.getWorld() && p.getLocation().distanceSquared(te.spawn) < 16 * 16) {
					pl.add(p);
				}
			}
		}

		return pl;
	}

	public static World lob() {
		return Bukkit.getWorld("BattleBaneLobby");
	}

	public static World are() {
		return Bukkit.getWorld("BattleBaneArena");
	}

	public static World areBuild() {
		return Bukkit.getWorld("BattleBaneArenaBuild");
	}

	public static World wor() {
		return Bukkit.getWorld("BattleBaneWorld");
	}

}
