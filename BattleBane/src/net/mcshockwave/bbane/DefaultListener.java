package net.mcshockwave.bbane;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.Currency.LevelUtils;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.bbane.kits.ArcherSettings;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class DefaultListener implements Listener {

	Random										rand		= new Random();

	public static HashMap<TNTPrimed, String>	demo		= new HashMap<>();

	public HashMap<Block, String>				pyro		= new HashMap<>();
	public HashMap<String, String>				pyroIgnite	= new HashMap<>();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		if (!BattleBane.started || BBTeam.getTeamFor(p) == null) {
			if (p.getGameMode() != GameMode.SURVIVAL) {
				p.setGameMode(GameMode.SURVIVAL);
			}
			p.teleport(BattleBane.lob().getSpawnLocation());
			BattleBane.resetPlayer(p, true);
			p.getInventory().addItem(
					ItemMetaUtils.setItemName(new ItemStack(Material.NETHER_STAR), "Class Selector"),
					ItemMetaUtils.setItemName(new ItemStack(Material.WOOL), "Team Selector"));
		}

		if (p.getWorld() == BattleBane.are() && BBTeam.getTeamFor(p) != null) {
			p.teleport(BBTeam.getTeamFor(p).getSpawn());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		onQuit(event.getPlayer());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		onQuit(event.getPlayer());
	}

	public void onQuit(Player p) {
		if (BattleBane.arena && p.getWorld() == BattleBane.are()) {
			BattleBane.resetPlayer(p, true);
			checkIfArenaDone();
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

					p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 100));
					p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 100));
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
			if (p.getWorld() == BattleBane.lob() && p.getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}

			if (ItemMetaUtils.hasLore(it) && ItemMetaUtils.getLoreArray(it)[0].equalsIgnoreCase("񘿾Kit Item")) {
				event.getItemDrop().remove();
				p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 2);
			}
		}
	}

	@EventHandler
	public void onEntityDeath(final EntityDeathEvent event) {
		final LivingEntity e = event.getEntity();

		if (e.getKiller() != null && BBKit.Medic.isKit(e.getKiller())) {
			Player k = e.getKiller();

			List<Player> nearby = new ArrayList<Player>();
			nearby.add(k);
			for (Entity en : k.getNearbyEntities(8, 8, 8)) {
				if (en instanceof Player) {
					Player n = (Player) en;

					if (BBTeam.getTeamFor(n) == BBTeam.getTeamFor(k)) {
						nearby.add(n);
					}
				}
			}

			double hp = 8 / nearby.size();
			for (Player n : nearby) {
				double hea = n.getHealth();
				hea += hp;
				if (hea > n.getMaxHealth()) {
					hea = n.getMaxHealth();
				}
				n.setHealth(hea);

				PacketUtils.playParticleEffect(ParticleEffect.HEART, n.getEyeLocation(), 1, 1, 10);
			}
		}

		if (e instanceof Giant) {
			ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1, (short) 1561);
			sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 50);
			event.getDrops().add(sword);
			for (int i = 0; i < rand.nextInt(8) + 4; i++) {
				event.getDrops().add(new ItemStack(Material.DIAMOND));
			}
			for (int i = 0; i < rand.nextInt(18) + 10; i++) {
				event.getDrops().add(new ItemStack(Material.IRON_INGOT));
			}
			event.setDroppedExp(rand.nextInt(200) + 100);

			e.getWorld().createExplosion(e.getLocation(), 8f);
			int i = 0;
			for (ItemStack item : event.getDrops()) {
				i++;
				final ItemStack it = item;
				Bukkit.getScheduler().runTaskLater(BattleBane.ins, new Runnable() {
					public void run() {
						Item i = e.getLocation().getWorld().dropItem(e.getLocation(), it);
						double rad = 0.4;
						i.setVelocity(new Vector(rand.nextGaussian() * rad, 0.2, rand.nextGaussian() * rad));
					}
				}, i * 3);
			}
			event.getDrops().clear();
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();

		if (p.getKiller() != null) {
			Player k = p.getKiller();
			Random rand = new Random();

			if (rand.nextInt(SQLTable.Settings.getInt("Setting", "XPChance", "Value")) == 0) {
				int minXp = SQLTable.Settings.getInt("Setting", "XPMin", "Value");
				int maxXp = SQLTable.Settings.getInt("Setting", "XPMax", "Value");
				LevelUtils.addXP(k, (rand.nextInt(maxXp - minXp) + minXp) * (k.getWorld() == BattleBane.wor() ? 2 : 1),
						"killing " + p.getName(), true);
			}
		}

		for (ItemStack it : event.getDrops().toArray(new ItemStack[0])) {
			if (it != null && it.getType() != Material.AIR) {
				if (it.getType() == Material.ARROW && BBKit.Archer.isKit(p)) {
					event.getDrops().remove(it);
				}
				if (ItemMetaUtils.hasLore(it) && ItemMetaUtils.getLoreArray(it)[0].equalsIgnoreCase("񘿾Kit Item")) {
					event.getDrops().remove(it);
				}
			}
		}

		if (p.getKiller() != null && BBKit.Medic.isKit(p.getKiller())) {
			Player k = p.getKiller();

			List<Player> nearby = new ArrayList<Player>();
			nearby.add(k);
			for (Entity e : k.getNearbyEntities(8, 8, 8)) {
				if (e instanceof Player) {
					Player n = (Player) e;

					if (BBTeam.getTeamFor(n) == BBTeam.getTeamFor(k)) {
						nearby.add(n);
					}
				}
			}

			double hp = 12 / nearby.size();
			for (Player n : nearby) {
				double hea = n.getHealth();
				hea += hp;
				if (hea > n.getMaxHealth()) {
					hea = n.getMaxHealth();
				}
				n.setHealth(hea);

				PacketUtils.playParticleEffect(ParticleEffect.HEART, n.getEyeLocation(), 1, 1, 10);
			}
		}

		pyroIgnite.remove(p.getName());

		if (BattleBane.arena && p.getWorld() == BattleBane.are()) {
			event.setDeathMessage("�8[ARENA�8] " + event.getDeathMessage());

			PlayerRespawnEvent ev = new PlayerRespawnEvent(p, BattleBane.lob().getSpawnLocation(), false);
			Bukkit.getPluginManager().callEvent(ev);
			for (ItemStack it : event.getDrops()) {
				p.getWorld().dropItemNaturally(p.getEyeLocation(), it);
			}
			event.getDrops().clear();
			BattleBane.resetPlayer(p, true);
			p.teleport(ev.getRespawnLocation());

			Bukkit.getScheduler().runTaskLater(BattleBane.ins, new Runnable() {
				public void run() {
					checkIfArenaDone();					
				}
			}, 1l);
		}
	}

	public void checkIfArenaDone() {
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
			if (a.name().contains("LEFT_CLICK")) {
				if (it.getType() == Material.BOW && ArcherSettings.isBow(it)) {
					ArcherSettings.getMenu().open(p);
				}
			}
		}
	}

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		Entity ee = event.getEntity();

		if (ee instanceof Player) {
			Player p = (Player) ee;

			if (ArcherSettings.isBow(event.getBow())) {
				ArcherSettings.getSetting(event.getBow()).onShoot(p, event);
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

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		final Block b = event.getBlock();

		if (p.getGameMode() != GameMode.CREATIVE && p.getWorld() == BattleBane.lob()) {
			event.setCancelled(true);
			return;
		}

		if (!canBuildBase(b) && (b.getType() == Material.MELON_BLOCK || b.getType() == Material.LOG)) {
			event.setCancelled(false);
			final Material mat = b.getType();
			final byte dura = b.getData();
			Bukkit.getScheduler().runTaskLater(BattleBane.ins, new Runnable() {
				public void run() {
					b.setType(mat);
					b.setData(dura);
				}
			}, 200);
			return;
		}

		if (!canBuildBase(b) || !canBuildCenter(b)) {
			event.setCancelled(true);
			return;
		}

		int mult = BBKit.Miner.isKit(p) ? 5 : 4;

		if (b.getType() == Material.IRON_ORE) {
			b.breakNaturally(null);
			event.setExpToDrop(4);
			for (int i = 0; i < mult; i++) {
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.IRON_INGOT));
			}
		}

		if (b.getType() == Material.GOLD_ORE) {
			b.breakNaturally(null);
			event.setExpToDrop(8);
			for (int i = 0; i < mult; i++) {
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.GOLD_INGOT));
			}
		}

		if (b.getType() == Material.COAL_ORE) {
			for (int i = 0; i < mult - 1; i++) {
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.COAL));
			}
		}

		if (b.getType() == Material.EMERALD_ORE) {
			for (int i = 0; i < mult - 1; i++) {
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.EMERALD));
			}
		}

		if (b.getType() == Material.REDSTONE_ORE) {
			for (int i = 0; i < (mult - 1) * 5; i++) {
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.REDSTONE));
			}
		}

		if (b.getType() == Material.LAPIS_ORE) {
			for (int i = 0; i < (mult - 1) * 7; i++) {
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.INK_SACK, 1, (short) 4));
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

		if (!canBuildBase(b) || !canBuildCenter(b)) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b : event.blockList().toArray(new Block[0])) {
			if (!canBuildBase(b) || !canBuildCenter(b, true)) {
				event.blockList().remove(b);
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack cu = event.getCurrentItem();
		Inventory i = event.getInventory();

		if (i.getType() == InventoryType.CHEST) {
			if (ItemMetaUtils.hasLore(cu) && ItemMetaUtils.getLoreArray(cu)[0].equalsIgnoreCase("񘿾Kit Item")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		Location to = event.getTo();
		Location fr = event.getFrom();

		if (to.getWorld() == BattleBane.lob() && to.getY() < 55 && p.getGameMode() != GameMode.CREATIVE) {
			BattleBane.resetPlayer(p, true);
			p.teleport(BattleBane.lob().getSpawnLocation());
			BBKit.giveSelectors(p);
		}

		BBTeam bbt = BBTeam.getTeamFor(p);
		if (BattleBane.isInThroneRoom(fr, bbt) && !BattleBane.isInThroneRoom(to, bbt)) {
			p.sendMessage("You have left your throne room!");
			p.playSound(p.getLocation(), Sound.NOTE_BASS, 10, 0);
		}
		if (!BattleBane.isInThroneRoom(fr, bbt) && BattleBane.isInThroneRoom(to, bbt)) {
			p.sendMessage("You have entered your throne room!");
			p.playSound(p.getLocation(), Sound.NOTE_PLING, 10, 1.5f);
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
		if (BattleBane.wor() != l.getWorld()) {
			return true;
		}
		for (BBTeam bbt : BBTeam.values()) {
			if (Math.abs(bbt.x - l.getBlockX()) < 20 && Math.abs(bbt.z - l.getBlockZ()) < 20
					&& l.getBlockY() > bbt.yorig.getScore() - 30) {
				return false;
			}
		}
		return true;
	}

	public static boolean canBuildCenter(Block b) {
		return canBuildCenter(b, false);
	}

	public static boolean canBuildCenter(Block b, boolean includeCenter) {
		Location l = b.getLocation();
		if (BattleBane.wor() != l.getWorld()) {
			return true;
		}
		if (Math.abs(l.getBlockX()) < 25
				&& Math.abs(l.getBlockZ()) < 25
				&& !(!includeCenter && l.getBlockY() > BattleBane.centerOrigin - 1 && Math.abs(l.getBlockX()) <= 5 && Math
						.abs(l.getBlockZ()) <= 5)) {
			return false;
		}
		return true;
	}
}
