package net.mcshockwave.bbane;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.bbane.teams.BBTeam;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class DefaultListener implements Listener {

	Random								rand		= new Random();

	public HashMap<TNTPrimed, String>	demo		= new HashMap<>();

	public HashMap<Block, String>		pyro		= new HashMap<>();
	public HashMap<String, String>		pyroIgnite	= new HashMap<>();

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

			if (isInFireByTeam(p)) {
				event.setCancelled(true);
				p.setFireTicks(0);
			}

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

					if (pyroIgnite.containsKey(p.getName())
							&& pyroIgnite.get(p.getName()).equalsIgnoreCase(d.getName()) && p.getFireTicks() > 0) {
						event.setDamage(event.getDamage() + 2);
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
			if (ItemMetaUtils.hasLore(it) && ItemMetaUtils.getLoreArray(it)[0].equalsIgnoreCase("񘿾Kit Item")) {
				event.getItemDrop().remove();
				p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 2);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();

		for (ItemStack it : event.getDrops().toArray(new ItemStack[0])) {
			if (it != null && it.getType() != Material.AIR) {
				if (ItemMetaUtils.hasLore(it) && ItemMetaUtils.getLoreArray(it)[0].equalsIgnoreCase("񘿾Kit Item")) {
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
					p2.sendMessage("You have 15 seconds to loot the body of " + p.getName());
				}
			}
		}

		if (pyroIgnite.containsKey(p.getName()) && p.getFireTicks() > 0) {
			p.getWorld().createExplosion(p.getLocation(), 0f);
			BBTeam t = BBTeam.getTeamFor(Bukkit.getOfflinePlayer(pyroIgnite.get(p.getName())));
			for (Entity e : p.getNearbyEntities(5, 5, 5)) {
				if (e instanceof Player && t.isTeam((Player) e)) {
					continue;
				}

				e.setFireTicks(e.getFireTicks() + 200);
			}
		}
		pyroIgnite.remove(p.getName());

		if (p.getWorld() == BattleBane.are() && BattleBane.arena) {
			event.setDeathMessage("�8[ARENA�8] " + event.getDeathMessage());

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
					event.setCancelled(true);
					BBKit.getClassMenu(p).open(p);
				}

				if (it.getType() == Material.WOOL && ItemMetaUtils.hasCustomName(it)) {
					if (BBKit.getClassFor(p) != null) {
						event.setCancelled(true);
						BBTeam.getTeamMenu(p).open(p);
					} else {
						p.sendMessage("Select a class before choosing a team!");
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockIgnite(BlockPlaceEvent event) {
		Block b = event.getBlock();
		Player p = event.getPlayer();

		if (b.getType() == Material.FIRE && BBKit.Pyro.isKit(p)) {
			pyro.remove(b);
			pyro.put(b, p.getName());

			b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.FIRE);
		}
	}

	@SuppressWarnings("deprecation")
	public boolean isInFireByTeam(Player p) {
		Block bl = p.getLocation().getBlock();
		BlockFace[] bfs = { null, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
				BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };

		for (int i = 0; i <= 1; i++) {
			for (BlockFace bf : bfs) {
				Block b;
				if (bf != null) {
					b = bl.getRelative(0, i, 0).getRelative(bf);
				} else
					b = bl.getRelative(0, i, 0);

				if (b.getType() == Material.FIRE && pyro.containsKey(b)) {
					String fire = pyro.get(b);

					BBTeam t = BBTeam.getTeamFor(Bukkit.getOfflinePlayer(fire));
					if (t != null && t.isTeam(p)) {
						return true;
					}

					pyroIgnite.remove(p.getName());
					pyroIgnite.put(p.getName(), fire);
				}
			}
		}
		return false;
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (!canBuildBase(event.getEntity().getLocation().getBlock())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (!canBuildBase(event.getTarget().getLocation().getBlock())) {
			event.setTarget(null);
		}
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
		Entity e = event.getEntity();
		if (e instanceof Player) {
			Player p = (Player) e;

			if (isInFireByTeam(p)) {
				event.setCancelled(true);
				return;
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

		if (!canBuildBase(b)) {
			event.setCancelled(true);
			return;
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

		if (BBKit.Demoman.isKit(p) && p.getItemInHand().getType() == Material.TNT) {
			event.setBuild(false);
			b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.TNT);

			ItemStack it = p.getItemInHand();
			if (it.getAmount() > 1) {
				it.setAmount(it.getAmount() - 1);
			} else {
				it.setType(Material.AIR);
			}
			p.setItemInHand(it);

			TNTPrimed tnt = (TNTPrimed) b.getWorld().spawnEntity(b.getLocation().add(0.5, 0.5, 0.5),
					EntityType.PRIMED_TNT);
			tnt.setFuseTicks(40);
			demo.put(tnt, p.getName());
			return;
		}

		if (!canBuildBase(b)) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b : event.blockList().toArray(new Block[0])) {
			if (!canBuildBase(b)) {
				event.blockList().remove(b);
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

	public static boolean canBuildBase(Block b) {
		Location l = b.getLocation();
		for (BBTeam bbt : BBTeam.values()) {
			if (Math.abs(bbt.x - l.getBlockX()) < 20 && Math.abs(bbt.z - l.getBlockZ()) < 20) {
				return false;
			}
		}
		return true;
	}
}
