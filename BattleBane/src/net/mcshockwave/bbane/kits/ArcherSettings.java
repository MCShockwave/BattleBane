package net.mcshockwave.bbane.kits;

import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.bbane.BattleBane;
import net.mcshockwave.bbane.DefaultListener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public enum ArcherSettings {

	Single_Shot(
		Material.ARROW,
		1),
	Shotbow(
		Material.ARROW,
		5),
	Burst(
		Material.ARROW,
		3),
	Bomb(
		Material.TNT,
		1);

	public Material	icon;
	public int		am;
	public String	name;

	private ArcherSettings(Material icon, int am) {
		this.icon = icon;
		this.am = am;
		this.name = name().replace('_', ' ');
	}

	public static final String	pre	= "§bArcher Bow §8- §6§o";

	public ItemStack setItemTo(ItemStack toSet) {
		ItemStack it = toSet.clone();
		ItemMetaUtils.setItemName(it, pre + name);
		ItemMetaUtils.setLore(it, "§6Kit Item", "§7Click to set mode");
		return it;
	}

	public static boolean isBow(ItemStack it) {
		if (ItemMetaUtils.hasCustomName(it)) {
			return ItemMetaUtils.getItemName(it).startsWith(pre);
		}
		return false;
	}

	public static ArcherSettings getSetting(ItemStack it) {
		String set = ItemMetaUtils.getItemName(it).replaceFirst(pre, "");
		return valueOf(set);
	}

	public static ItemMenu getMenu() {
		ItemMenu im = new ItemMenu("Archer Bow", values().length);

		for (int i = 0; i < values().length; i++) {
			final ArcherSettings as = values()[i];

			Button b = new Button(true, as.icon, as.am, 0, as.name, "", "Click to use");
			b.setOnClick(new ButtonRunnable() {
				public void run(Player p, InventoryClickEvent event) {
					if (p.getItemInHand().getType() != Material.BOW) {
						return;
					}
					p.setItemInHand(as.setItemTo(p.getItemInHand()));
				}
			});
			im.addButton(b, i);
		}

		return im;
	}

	@SuppressWarnings("deprecation")
	public void onShoot(final Player p, final EntityShootBowEvent event) {
		final PlayerInventory pi = p.getInventory();

		if (this == Shotbow) {
			event.setCancelled(true);

			for (int i = 0; i < 5; i++) {
				if (pi.contains(Material.ARROW)) {
					Vector v = p.getEyeLocation().getDirection();
					Arrow a = p.getWorld().spawnArrow(p.getEyeLocation().add(v), v, event.getForce() * 1.5f, 15);
					a.setKnockbackStrength(0);
					a.setShooter(p);
					a.setCritical(((Arrow) event.getProjectile()).isCritical());
					((CraftArrow) a).getHandle().fromPlayer = 1;

					pi.removeItem(new ItemStack(Material.ARROW));
				}
			}
			p.updateInventory();
		}

		if (this == Burst) {
			for (int i = 1; i < 3; i++) {
				Bukkit.getScheduler().runTaskLater(BattleBane.ins, new Runnable() {
					public void run() {
						if (pi.contains(Material.ARROW)) {
							Vector v = p.getEyeLocation().getDirection();
							Arrow a = p.getWorld().spawnArrow(p.getEyeLocation().add(v), v, event.getForce() * 3, 5);
							a.setKnockbackStrength(0);
							a.setShooter(p);
							a.setCritical(((Arrow) event.getProjectile()).isCritical());
							((CraftArrow) a).getHandle().fromPlayer = 1;

							pi.removeItem(new ItemStack(Material.ARROW));
							p.updateInventory();
						}
					}
				}, i * 5);
			}
		}

		if (this == Bomb) {
			if (pi.contains(Material.SULPHUR, 3)) {
				pi.removeItem(new ItemStack(Material.SULPHUR, 3));
				pi.addItem(new ItemStack(Material.ARROW));
				p.updateInventory();

				TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.PRIMED_TNT);
				tnt.setVelocity(p.getEyeLocation().getDirection().multiply(event.getForce() * 3));
				DefaultListener.demo.put(tnt, p.getName());
				event.setProjectile(tnt);
			} else {
				p.sendMessage("§cOut of gunpowder! (You need 3)");
				event.setCancelled(true);
			}
		}
	}

}
