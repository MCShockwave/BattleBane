package net.mcshockwave.bbane;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.bbane.teams.BBTeam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity ee = event.getEntity();

		if (ee instanceof Player) {
			Player p = (Player) ee;

			if (event instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
				Entity de = ev.getDamager();

				if (ev.getDamager() instanceof Player) {
					Player d = (Player) de;

					if (!pvpEnabled(p, d)) {
						event.setCancelled(true);
					}
				}
			} else {
				if (!pvpEnabled(p, null)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		final Player p = event.getPlayer();

		if (BattleBane.started) {
			if (BBTeam.getTeamFor(p) != null) {
				event.setRespawnLocation(BBTeam.getTeamFor(p).spawn);
			} else
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
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();

		if (BBKit.Demoman.isKit(p)) {
			TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getLocation().add(0.5, 1.5, 0.5),
					EntityType.PRIMED_TNT);
			tnt.setFuseTicks(300);

			for (Player p2 : Bukkit.getOnlinePlayers()) {
				if (p2.getWorld() == p.getWorld() && p2.getLocation().distanceSquared(p.getLocation()) < 16 * 16) {
					p2.sendMessage("§cYou have 15 seconds to loot the body of " + p.getName());
				}
			}
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
					event.setCancelled(true);

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

				if (it.getType() == Material.WOOL && ItemMetaUtils.hasCustomName(it)) {
					if (BBKit.getClassFor(p) != null) {
						ItemMenu cl = new ItemMenu("Team Selection", BBTeam.values().length);
						event.setCancelled(true);

						for (int i = 0; i < BBTeam.values().length; i++) {
							final BBTeam t = BBTeam.values()[i];
							Button b = new Button(true, Material.WOOL, 1, t.data, t.c + t.name(), "", "Click to join",
									t.team.getPlayers().size() + " players");
							b.setOnClick(new ButtonRunnable() {
								public void run(Player p, InventoryClickEvent event) {
									MCShockwave.send(t.c, p, "Joined team %s", t.name());
									t.addPlayer(p);
								}
							});

							cl.addButton(b, i);
						}

						cl.open(p);
					} else {
						p.sendMessage("§cSelect a class before choosing a team!");
					}
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

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();

		if (BBKit.Demoman.isKit(p) && b.getType() == Material.TNT) {
			b.setType(Material.AIR);
			b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.TNT);

			TNTPrimed tnt = (TNTPrimed) b.getWorld().spawnEntity(b.getLocation().add(0.5, 0.5, 0.5),
					EntityType.PRIMED_TNT);
			tnt.setFuseTicks(40);
		}
	}

	public static boolean pvpEnabled(Player p, Player d) {
		if (d == null) {
			if (p.getWorld() == BattleBane.lob()) {
				return false;
			}
		} else {
			if (BBTeam.getTeamFor(p) == null || BBTeam.getTeamFor(d) == null) {
				return false;
			}
			if (p.getWorld() == BattleBane.lob() || d.getWorld() == BattleBane.lob()) {
				return false;
			}
		}
		return true;
	}
}
