package net.mcshockwave.bbane;

import net.mcshockwave.bbane.teams.BBTeam;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;

public enum Arena {

	Arena_0(
		v(0, 30, -38),
		v(39, 30, 0),
		v(0, 30, 39),
		v(0, 30, -38));

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

	private static Vector v(double x, double y, double z) {
		return new Vector(x + 0.5, y, z + 0.5);
	}

}
