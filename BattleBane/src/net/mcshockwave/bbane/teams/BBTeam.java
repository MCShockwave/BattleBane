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
		ChatColor.RED),
	Blue(
		1000,
		-1000,
		ChatColor.AQUA),
	Yellow(
		-1000,
		1000,
		ChatColor.YELLOW),
	Green(
		-1000,
		-1000,
		ChatColor.GREEN);

	public int			x;
	public int			z;
	public Location spawn;
	public ChatColor	c;
	
	public Team team;

	private BBTeam(int x, int z, ChatColor c) {
		this.x = x;
		this.z = z;
		this.c = c;
		
		this.spawn = new Location(BattleBane.wor(), x, 64, z);
		
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
		p.teleport(spawn);
	}
	
	public void removePlayer(Player p) {
		team.removePlayer(p);
		p.teleport(BattleBane.lob().getSpawnLocation());
	}

}
