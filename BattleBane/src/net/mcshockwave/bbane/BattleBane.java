package net.mcshockwave.bbane;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleBane extends JavaPlugin {
	
	public static BattleBane ins;

	public void onEnable() {
		ins = this;
		Bukkit.getPluginManager().registerEvents(new DefaultListener(), this);
	}
	
}
