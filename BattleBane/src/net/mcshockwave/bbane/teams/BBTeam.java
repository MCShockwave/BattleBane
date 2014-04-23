package net.mcshockwave.bbane.teams;

import net.mcshockwave.bbane.BBKit;
import net.mcshockwave.bbane.BattleBane;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public enum BBTeam {

	Red(
		500,
		500,
		ChatColor.RED,
		14),
	Blue(
		500,
		-500,
		ChatColor.AQUA,
		3),
	Yellow(
		-500,
		500,
		ChatColor.YELLOW,
		4),
	Green(
		-500,
		-500,
		ChatColor.GREEN,
		5);

	public int			x;
	public int			z;
	public Location		spawn;
	public ChatColor	c;
	public short		data;

	public Team			team;

	private BBTeam(int x, int z, ChatColor c, int data) {
		this.x = x;
		this.z = z;
		this.c = c;

		this.data = (short) data;

		this.spawn = new Location(BattleBane.wor(), x + 0.5, 64, z + 0.5);

		Team t = BattleBane.score.getTeam(name());
		if (t != null) {
			t.unregister();
		}

		team = BattleBane.score.registerNewTeam(name());
		team.setPrefix(c.toString());
		team.setSuffix("§r");
		team.setAllowFriendlyFire(false);
		team.setCanSeeFriendlyInvisibles(true);
	}

	public void addPlayer(Player p) {
		team.addPlayer(p);
		spawn.getChunk().load();
		p.teleport(spawn);

		if (BBKit.getClassFor(p) != null) {
			BBKit.getClassFor(p).giveKit(p);
		}
	}

	public static void removePlayer(Player p) {
		Team pt = BattleBane.score.getPlayerTeam(p);
		if (pt != null) {
			pt.removePlayer(p);
		}
		BattleBane.lob().getSpawnLocation().getChunk().load();
		p.teleport(BattleBane.lob().getSpawnLocation());
	}

	public static BBTeam getTeamFor(OfflinePlayer p) {
		for (BBTeam t : values()) {
			if (t.team.hasPlayer(p)) {
				return t;
			}
		}
		return null;
	}

	public boolean isTeam(OfflinePlayer p) {
		try {
			if (getTeamFor(p) == null) {
				return false;
			}
			return getTeamFor(p) == this;
		} catch (Exception e) {
			return false;
		}
	}

}
