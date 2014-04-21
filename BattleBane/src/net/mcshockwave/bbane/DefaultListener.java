package net.mcshockwave.bbane;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class DefaultListener implements Listener {

	Random	rand	= new Random();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		if (!BattleBane.started) {
			p.teleport(BattleBane.lob().getSpawnLocation());
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		final Player p = event.getPlayer();

		if (BattleBane.started) {
			event.setRespawnLocation(BattleBane.wor().getSpawnLocation());

			Bukkit.getScheduler().runTaskLater(BattleBane.ins, new Runnable() {
				public void run() {
					BBKit c = BBKit.getClassFor(p);
					c.giveKit(p);
				}
			}, 10l);
		} else {
			event.setRespawnLocation(BattleBane.lob().getSpawnLocation());
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();

		if (it != null && it.getType() != Material.AIR) {
			if (a.name().contains("RIGHT_CLICK")) {
				if (it.getType() == Material.NETHER_STAR && ItemMetaUtils.hasCustomName(it)) {
					ItemMenu cl = new ItemMenu("Classes", BBKit.values().length);

					for (int i = 0; i < BBKit.values().length; i++) {
						final BBKit cs = BBKit.values()[i];
						Button b = new Button(true, cs.ico, cs.am, cs.da, cs.name, "", "Click to use");
						b.setOnClick(new ButtonRunnable() {
							public void run(Player p, InventoryClickEvent event) {
								cs.onUse(p);
								MCShockwave.send(ChatColor.GREEN, p, "Used class %s", cs.name);
							}
						});

						cl.addButton(b, i);
					}

					cl.open(p);
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();

		if (BBKit.Miner.isKit(p)) {
			event.setCancelled(true);
			Material or = b.getType();

			if ((or == Material.IRON_ORE || or == Material.GOLD_ORE) && rand.nextInt(4) == 0) {
				if (or == Material.IRON_ORE) {
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.IRON_INGOT));
				}
				if (or == Material.GOLD_ORE) {
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.GOLD_INGOT));
				}
				b.getWorld().playEffect(b.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
				b.breakNaturally(null);
			} else {
				b.breakNaturally(p.getItemInHand().clone());
			}
		}
	}
}
