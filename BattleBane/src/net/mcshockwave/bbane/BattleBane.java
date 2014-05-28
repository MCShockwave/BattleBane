package net.mcshockwave.bbane;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.bbane.commands.Bane;
import net.mcshockwave.bbane.commands.BuildWorld;
import net.mcshockwave.bbane.commands.ClassCmd;
import net.mcshockwave.bbane.commands.PrivateChest;
import net.mcshockwave.bbane.commands.Surface;
import net.mcshockwave.bbane.teams.BBTeam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class BattleBane extends JavaPlugin {

	protected static final int	ARENA_TIME		= 600;

	static Random				rand			= new Random();

	public static BattleBane	ins;

	public static Scoreboard	score;

	public static boolean		started			= true, arena = false;

	public static boolean		autoArena		= false;

	public static Arena			currentArena	= null;

	public static int			pointsNeeded	= 5;

	public static int			centerOrigin	= 64;

	public static Score			time;

	@SuppressWarnings("deprecation")
	public void onEnable() {
		score = Bukkit.getScoreboardManager().getMainScoreboard();

		ins = this;
		Bukkit.getPluginManager().registerEvents(new DefaultListener(), this);

		score.resetScores(Bukkit.getOfflinePlayer("§dTime:"));
		time = score.getObjective("Points").getScore(Bukkit.getOfflinePlayer("§dTime:"));

		saveDefaultConfig();

		for (Player p : Bukkit.getOnlinePlayers()) {
			resetPlayer(p, true);
			p.teleport(lob().getSpawnLocation());
			BBKit.giveSelectors(p);
		}

		MCShockwave.min = Rank.OBSIDIAN;

		for (BBTeam bbt : BBTeam.values()) {
			bbt.setOrigin();
			bbt.points.setScore(0);
		}

		Score max = score.getObjective("Points").getScore(Bukkit.getOfflinePlayer("§7 Points Needed"));
		max.setScore(pointsNeeded);

		getCommand("bane").setExecutor(new Bane());
		getCommand("surface").setExecutor(new Surface());
		getCommand("buildworld").setExecutor(new BuildWorld());
		getCommand("class").setExecutor(new ClassCmd());
		getCommand("chest").setExecutor(new PrivateChest());

		new WorldCreator("BattleBaneLobby").type(WorldType.FLAT).createWorld();
		new WorldCreator("BattleBaneArena").type(WorldType.FLAT).createWorld();
		new WorldCreator("BattleBaneArenaBuild").type(WorldType.FLAT).createWorld();
		genWorld();

		centerOrigin = wor().getHighestBlockYAt(0, 0);
	}

	public void onDisable() {
	}

	public static void reset() {
		Bukkit.broadcastMessage("§cRESETTING WORLD, PREPARE FOR LAG");
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.teleport(lob().getSpawnLocation());
		}

		deleteWorld(wor());

		genWorld();
	}

	public static void deleteWorld(String w) {
		if (Bukkit.unloadWorld(w, false)) {
			System.out.println("Unloaded world");
		} else {
			System.err.println("Couldn't unload world");
		}
		if (delete(new File(w))) {
			System.out.println("Deleted world!");
		} else {
			System.err.println("Couldn't delete world");
		}
	}

	public static void deleteWorld(World w) {
		deleteWorld(w.getName());
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

	public static void copyWorld(String s, String t) {
		File source = new File(s);
		File target = new File(t);
		try {
			copyTest(source, target);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copyTest(File src, File dest) throws IOException {
		if (src.isDirectory()) {

			if (!dest.exists()) {
				dest.mkdir();
			}

			String files[] = src.list();

			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyTest(srcFile, destFile);
			}

		} else {
			if (src.getName().equalsIgnoreCase("uid.dat")) {
				return;
			}

			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
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

		if (wor().getSpawnLocation().getBlockX() == 0 && wor().getSpawnLocation().getBlockZ() == 0) {
			return;
		}

		for (BBTeam bbt : BBTeam.values()) {
			bbt.setOrigin();
		}

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

		loadSchematic("bb_center", cen.getLocation());
		centerOrigin = cen.getLocation().getBlockY();

		for (BBTeam t : BBTeam.values()) {
			Block b = t.getSchemOrigin().getBlock();
			b.getChunk().load();

			loadSchematic("bb_" + t.name().toLowerCase(), b.getLocation());
		}
	}

	public static ArrayList<BukkitTask>	arenaTasks	= new ArrayList<>();

	public static void startArenaCount(final int startTime) {
		final int[] timeBroad = { 300, 180, 120, 60, 45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

		for (int i : timeBroad) {
			if (i > startTime) {
				continue;
			}
			final int timeLeft = i;
			arenaTasks.add(Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
				public void run() {
					if (timeLeft > 60) {
						MCShockwave.broadcast(ChatColor.YELLOW, "%s minutes until the arena starts!", timeLeft / 60);
					} else
						MCShockwave.broadcast(ChatColor.YELLOW, "%s seconds until the arena starts!", timeLeft);
					wor().playSound(wor().getSpawnLocation(), Sound.ORB_PICKUP, 10000, 2);
				}
			}, (startTime - i) * 20));
		}

		time.setScore(startTime);

		for (int i = 0; i < startTime; i++) {
			final int timeLeft = startTime - i;
			arenaTasks.add(Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
				public void run() {
					if (timeLeft > 10) {
						time.setScore(timeLeft);
					} else if (timeLeft == 10) {
						score.resetScores(time.getPlayer());
					}
				}
			}, i * 20));
		}

		arenaTasks.add(Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				startArena();
			}
		}, startTime * 20));
	}

	public static void stopArenaCount() {
		for (BukkitTask bt : arenaTasks) {
			bt.cancel();
		}

		arenaTasks.clear();
	}

	public static void startArena() {
		int teams = 0;
		for (BBTeam bbt : BBTeam.values()) {
			if (getArenaReady(bbt).size() > 0) {
				teams++;
			}
		}
		if (teams <= 1) {
			MCShockwave.broadcast("Not enough %s for the Arena!", "teams");
			return;
		}

		arena = true;

		Arena ar = Arena.values()[rand.nextInt(Arena.values().length)];

		currentArena = ar;

		for (BBTeam bbt : BBTeam.values()) {
			List<Player> ready = getArenaReady(bbt);
			if (ready.size() > 0) {
				for (Player p : ready) {
					ar.teleport(p, bbt);
				}
			}
		}

		MCShockwave.broadcast("Arena started on %s", ar.name);
	}

	public static void endArena(final BBTeam winner) {
		arena = false;

		for (Entity e : are().getEntities()) {
			if (e instanceof Player) {
				Player p = (Player) e;

				if (BBTeam.getTeamFor(p) != null) {
					resetPlayer(p, true);
					BBKit.getClassFor(p).giveKit(p);
					p.teleport(BBTeam.getTeamFor(p).getSpawn());
				} else {
					p.teleport(lob().getSpawnLocation());
				}
			}
		}

		if (winner != null) {
			MCShockwave.broadcast(winner.c, "%s has won on arena %s", winner.name(), currentArena.name);
			winner.points.setScore(winner.points.getScore() + 1);
			if (winner.points.getScore() >= pointsNeeded) {
				Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
					public void run() {
						for (int i = 0; i < 4; i++)
							Bukkit.broadcastMessage("");
						Bukkit.broadcastMessage(winner.c + "§l§m-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
						Bukkit.broadcastMessage(winner.c + "§l" + winner.name().toUpperCase() + " TEAM WINS THE GAME!");
						Bukkit.broadcastMessage(winner.c + "§lServer restarting soon!");
						Bukkit.broadcastMessage(winner.c + "§l§m-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
						for (int i = 0; i < 3; i++)
							Bukkit.broadcastMessage("");
					}
				}, 2);
			}
		} else {
			MCShockwave.broadcast("%s has won on arena %s", "Nobody", currentArena.name);
		}

		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				resetArena(winner.points.getScore() >= pointsNeeded);
			}
		}, 100l);

		currentArena = null;
	}

	public static void restartCountdown(int time) {
		int[] br = { 60, 45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

		for (final int i : br) {
			if (i >= time) {
				continue;
			}

			int timeSec = time - i;
			Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
				public void run() {
					MCShockwave.broadcast(ChatColor.YELLOW, "Server restarting in %s second" + (i != 1 ? "s" : ""), i);
				}
			}, timeSec * 20);
		}

		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§e§l§oSERVER RESTARTING!");
			}
		}, time * 20);

		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				Bane.restart();
			}
		}, (time * 20) + 10);
	}

	public static void resetArena(final boolean win) {
		System.out.println("Deleting arena file...");
		deleteWorld(are());

		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				System.out.println("Copying world files...");

				copyWorld("BattleBaneArenaBackup", "BattleBaneArena");
			}
		}, 30l);

		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				System.out.println("Saving all worlds...");

				for (World w : Bukkit.getWorlds()) {
					w.save();
				}

				System.out.println("Loading arena world...");

				new WorldCreator("BattleBaneArena").type(WorldType.FLAT).createWorld();

				if (are().getBlockAt(0, 0, 0).getType() != Material.AIR) {
					sendToMods("§cError resetting arena map... trying again");
					resetArena(win);
					return;
				} else {
					sendToMods("§aArena successfully generated");

					if (autoArena && !win) {
						startArenaCount(ARENA_TIME);

						generateCenter();
					}
				}

				if (win) {
					restartCountdown(15);
				}

				System.out.println("Done resetting world!");
			}
		}, 80l);
	}

	public static void sendToMods(String mes) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (SQLTable.hasRank(p.getName(), Rank.JR_MOD)) {
				p.sendMessage(mes);
			}
		}
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
				if (p.getWorld() == te.getSpawn().getWorld()
						&& p.getLocation().distanceSquared(te.getThroneRoom()) < 16 * 16
						&& p.getLocation().getY() > te.getThroneRoom().getY()) {
					pl.add(p);
				}
			}
		}

		return pl;
	}

	public static void generateCenter() {
		CenterEvent ce = CenterEvent.values()[rand.nextInt(CenterEvent.values().length)];
		ce.onStart();
		MCShockwave.broadcast(ChatColor.DARK_AQUA, "The center has spawned %s!", ce.name);
	}

	public static void resetPlayer(Player p) {
		resetPlayer(p, false);
	}

	public static void resetPlayer(Player p, boolean clearInv) {
		if (clearInv) {
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.setExp(0);
			p.setLevel(0);
		}
		p.setHealth(20f);
		p.setFireTicks(0);
		p.setFallDistance(0);
		p.setFoodLevel(20);
		p.setSaturation(10f);
		for (PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
	}

	public static void loadSchematic(String name, Location l) {
		File f = new File(ins.getDataFolder(), name + ".schematic");

		if (!f.exists()) {
			Bukkit.broadcastMessage("§cSchematic not found: " + name + ".schematic");
			return;
		}

		SchematicFormat schematic = SchematicFormat.getFormat(f);

		EditSession session = new EditSession(new BukkitWorld(l.getWorld()), Integer.MAX_VALUE);
		try {
			CuboidClipboard clipboard = schematic.load(f);
			clipboard.paste(session, BukkitUtil.toVector(l), false);
			session.flushQueue();
		} catch (Exception e) {
			Bukkit.broadcastMessage("§cError while loading schem: " + e.getMessage());
			e.printStackTrace();
		}
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
