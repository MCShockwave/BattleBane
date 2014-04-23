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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class DefaultListener implements Listener {

	Random	rand	= new Random();
	
	public HashMap<TNTPrimed, String> demo = new HashMap<>();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		if (!BattleBane.started || BBTeam.getTeamFor(p) == null) {
			if (p.getGameMode() != GameMode.SURVIVAL) {
				p.setGameMode(GameMode.SURVIVAL);
			}
			p.teleport(BattleBane.lob().getSpawnLocation());
			BattleBane.resetPlayer(p, true);
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.NETHER_STAR), "Class Selector"),
					ItemMetaUtils.setItemName(new ItemStack(Material.WOOL), "Team Selector"));
		}

		if (p.getWorld() == BattleBane.are() && BBTeam.getTeamFor(p) != null) {
			p.teleport(BBTeam.getTeamFor(p).getSpawn());
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity ee = event.getEntity();

		if (ee instanceof Player) {
			Player p = (Player) ee;

			if (event instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
				Entity de = ev.getDamager();
				
				if (de instanceof TNTPrimed && demo.containsKey(de)) {
					OfflinePlayer op = Bukkit.getOfflinePlayer(demo.get(de));
					if (BBTeam.getTeamFor(op) == BBTeam.getTeamFor(p)) {
						event.setCancelled(true);
					}
				}

				if (de instanceof Player) {
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
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		HumanEntity p = event.getEntity();
		
		if (p.getWorld() == BattleBane.lob()) {
			event.setFoodLevel(20);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		final Player p = event.getPlayer();

		if (BattleBane.started) {
			if (BBTeam.getTeamFor(p) != null) {
				event.setRespawnLocation(BBTeam.getTeamFor(p).getSpawn());
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
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player p = event.getPlayer();
		ItemStack it = event.getItemDrop().getItemStack();

		if (it != null && it.getType() != Material.AIR) {
			if (ItemMetaUtils.hasLore(it) && ItemMetaUtils.getLoreArray(it)[0].equalsIgnoreCase("�7�6Kit Item")) {
				event.getItemDrop().remove();
				p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 2);
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();

		for (ItemStack it : event.getDrops().toArray(new ItemStack[0])) {
			if (it != null && it.getType() != Material.AIR) {
				if (ItemMetaUtils.hasLore(it) && ItemMetaUtils.getLoreArray(it)[0].equalsIgnoreCase("�7�6Kit Item")) {
					event.getDrops().remove(it);
				}
			}
		}

		if (BBKit.Demoman.isKit(p) && p.getWorld() != BattleBane.are()) {
			TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getLocation().add(0.5, 1.5, 0.5),
					EntityType.PRIMED_TNT);
			tnt.setFuseTicks(300);
			demo.put(tnt, p.getName());

			for (Player p2 : Bukkit.getOnlinePlayers()) {
				if (p2.getWorld() == p.getWorld() && p2.getLocation().distanceSquared(p.getLocation()) < 16 * 16) {
					p2.sendMessage("�cYou have 15 seconds to loot the body of " + p.getName());
				}
			}
		}

		if (p.getWorld() == BattleBane.are() && BattleBane.arena) {
			event.setDeathMessage("�8[�a�lARENA�8] �f" + event.getDeathMessage());

			PlayerRespawnEvent ev = new PlayerRespawnEvent(p, BattleBane.lob().getSpawnLocation(), false);
			Bukkit.getPluginManager().callEvent(ev);
			BattleBane.resetPlayer(p, true);
			p.teleport(ev.getRespawnLocation());

			int tleft = 0;
			BBTeam win = null;
			for (BBTeam bbt : BBTeam.values()) {
				if (BattleBane.getAllInArena(bbt).size() > 0) {
					win = bbt;
					tleft++;
				}
			}

			if (tleft < 2) {
				BattleBane.endArena(win);
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
						p.sendMessage("�cSelect a class before choosing a team!");
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();
		
		if (p.getGameMode() != GameMode.CREATIVE && p.getWorld() == BattleBane.lob()) {
			event.setCancelled(true);
			return;
		}

		for (BBTeam bbt : BBTeam.values()) {
			if (b.getLocation().distanceSquared(bbt.getSpawn()) <= 50 * 50) {
				event.setCancelled(true);
				return;
			}
		}

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
		
		if (p.getGameMode() != GameMode.CREATIVE && p.getWorld() == BattleBane.lob()) {
			event.setCancelled(true);
			return;
		}
		
		if (BBKit.Demoman.isKit(p) && b.getType() == Material.TNT) {
			b.setType(Material.AIR);
			b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.TNT);

			TNTPrimed tnt = (TNTPrimed) b.getWorld().spawnEntity(b.getLocation().add(0.5, 0.5, 0.5),
					EntityType.PRIMED_TNT);
			tnt.setFuseTicks(40);
			demo.put(tnt, p.getName());
			return;
		}

		for (BBTeam bbt : BBTeam.values()) {
			if (b.getLocation().distanceSquared(bbt.getSpawn()) <= 50 * 50) {
				event.setCancelled(true);
			}
			return;
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (BBTeam bbt : BBTeam.values()) {
			for (Block b : event.blockList().toArray(new Block[0])) {
				if (b.getLocation().distanceSquared(bbt.getSpawn()) <= 50 * 50) {
					event.blockList().remove(b);
				}
			}
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
