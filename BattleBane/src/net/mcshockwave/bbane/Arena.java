package net.mcshockwave.bbane;

import net.mcshockwave.bbane.teams.BBTeam;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;

public enum Arena {

	Arena_1(
		v(-35, 30, -0),
		v(36, 30, 0),
		v(0, 30, 36),
		v(0, 30, -35));

	// 0 = Red, 1 = Blue, 2 = Yellow, 3 = Green

	public HashMap<BBTeam, Location>	spawns	= new HashMap<>();
	public String						name;

	Arena(Vector... vs) {
		for (int i = 0; i < vs.length; i++) {
			Vector v = vs[i];
			spawns.put(BBTeam.values()[i], new Location(BattleBane.are(), v.getX(), v.getY(), v.getZ()));
		}

		name = name().replace('_', ' ');
	}
	
	public void teleport(Player p, BBTeam bbt) {
		Location tp = spawns.get(bbt);
		tp.setWorld(BattleBane.are());
		p.teleport(tp);
	}

	private static Vector v(double x, double y, double z) {
		return new Vector(x + 0.5, y, z + 0.5);
	}

}
