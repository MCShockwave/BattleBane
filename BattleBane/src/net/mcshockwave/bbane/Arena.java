package net.mcshockwave.bbane;

import net.mcshockwave.bbane.teams.BBTeam;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;

public enum Arena {

	Mountains(
		v(50, 24, -73),
		v(63, 24, 34),
		v(-27, 24, 56),
		v(-67, 24, -69)),
	Jungle(
		v(304, 22, 87),
		v(314, 23, -9),
		v(233, 26, -18),
		v(240, 22, 70));

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
