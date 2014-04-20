package net.mcshockwave.bbane;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DefaultListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		p.teleport(BattleBane.lob().getSpawnLocation());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		try {
			event.setRespawnLocation(BattleBane.wor().getSpawnLocation());
		} catch (Exception e) {
			event.setRespawnLocation(BattleBane.lob().getSpawnLocation());
		}
	}

}
