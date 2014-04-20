package net.mcshockwave.bbane.teams;

import org.bukkit.ChatColor;

public enum Team {

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
	public ChatColor	c;

	private Team(int x, int z, ChatColor c) {
		this.x = x;
		this.z = z;
		this.c = c;
	}

}
