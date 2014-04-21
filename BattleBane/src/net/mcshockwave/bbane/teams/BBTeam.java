package net.mcshockwave.bbane.teams;

import net.mcshockwave.bbane.BattleBane;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public enum BBTeam {

	Red(
		1000,
		1000,
		ChatColor.RED,
		14),
	Blue(
		1000,
		-1000,
		ChatColor.AQUA,
		3),
	Yellow(
		-1000,
		1000,
		ChatColor.YELLOW,
		4),
	Green(
		-1000,
		-1000,
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
	}

	public static void removePlayer(Player p) {
		Team pt = BattleBane.score.getPlayerTeam(p);
		if (pt != null) {
			pt.removePlayer(p);
		}
		BattleBane.lob().getSpawnLocation().getChunk().load();
		p.teleport(BattleBane.lob().getSpawnLocation());
	}

	public static BBTeam getTeamFor(Player p) {
		for (BBTeam t : values()) {
			if (t.isTeam(p)) {
				return t;
			}
		}
		return null;
	}

	public boolean isTeam(Player p) {
		if (BattleBane.score.getPlayerTeam(p) == null) {
			return false;
		}
		return getTeamFor(p) == this;
	}

}
