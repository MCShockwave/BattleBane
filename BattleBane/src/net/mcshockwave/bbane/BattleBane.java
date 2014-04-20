package net.mcshockwave.bbane;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleBane extends JavaPlugin {
	
	public static BattleBane ins;

	public void onEnable() {
		ins = this;
		Bukkit.getPluginManager().registerEvents(new DefaultListener(), this);
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
