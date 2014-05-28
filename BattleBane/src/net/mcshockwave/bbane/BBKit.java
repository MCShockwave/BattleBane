package net.mcshockwave.bbane;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.bbane.kits.ArcherSettings;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public enum BBKit {

	Civilian(
		Material.WORKBENCH,
		1,
		0),
	Miner(
		Material.IRON_PICKAXE,
		1,
		0,
		new ItemStack(Material.WOOD_PICKAXE)),
	Demoman(
		Material.TNT,
		3,
		0,
		new ItemStack(Material.TNT, 3)),
	Pyro(
		Material.FLINT_AND_STEEL,
		1,
		0,
		new ItemStack(Material.FLINT_AND_STEEL)),
	Medic(
		Material.GOLDEN_APPLE,
		1,
		0),
	Archer(
		Material.BOW,
		1,
		0,
		new ItemStack(Material.BOW),
		new ItemStack(Material.ARROW, 15));

	protected static HashMap<String, BBKit>	used	= new HashMap<>();

	public Material							ico;
	public int								am, da;
	public String							name;

	public ItemStack[]						kit;

	private BBKit(Material ico, int am, int da, ItemStack... items) {
		this.ico = ico;
		this.am = am;
		this.da = da;

		this.kit = items;

		this.name = name().replace('_', ' ');
	}

	public static BBKit getClassFor(Player p) {
		return getClassFor(p.getName());
	}

	public static BBKit getClassFor(String s) {
		for (BBKit cs : values()) {
			if (cs.isKit(s)) {
				return cs;
			}
		}
		return null;
	}

	public static void giveSelectors(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.NETHER_STAR), "§rClass Selector"),
				ItemMetaUtils.setItemName(new ItemStack(Material.WOOL), "§rTeam Selector"));
	}

	public void onUse(Player p) {
		used.remove(p.getName());
		used.put(p.getName(), this);
	}

	public void giveKit(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		for (ItemStack it : kit) {
			if (it.getType() == Material.ARROW) {
				p.getInventory().addItem(it);
				continue;
			}
			p.getInventory().addItem(ItemMetaUtils.setLore(it, "§6Kit Item"));
		}
		p.getInventory().addItem(ItemMetaUtils.setLore(new ItemStack(Material.COOKED_BEEF, 8), "§6Kit Item"));

		if (this == Archer) {
			p.getInventory().setItem(0, ArcherSettings.Single_Shot.setItemTo(p.getInventory().getItem(0)));
		}

		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, this == Civilian ? 1 : 0));
	}

	public boolean isKit(Player p) {
		return isKit(p.getName());
	}

	public boolean isKit(String s) {
		if (!used.containsKey(s)) {
			return false;
		}
		return used.get(s) == this;
	}

	public static ItemMenu getClassMenu(Player p) {
		return getClassMenu(p, false);
	}

	public static ItemMenu getClassMenu(Player p, final boolean kill) {
		ItemMenu cl = new ItemMenu("Classes", BBKit.values().length);

		for (int i = 0; i < BBKit.values().length; i++) {
			final BBKit cs = BBKit.values()[i];
			Button b = new Button(true, cs.ico, cs.am, cs.da, cs.name, "", "Click to use");
			b.setOnClick(new ButtonRunnable() {
				public void run(Player p, InventoryClickEvent event) {
					cs.onUse(p);
					MCShockwave.send(ChatColor.GREEN, p, "Used class %s", cs.name);
					if (kill) {
						p.damage(p.getHealth());
					}
				}
			});

			cl.addButton(b, i);
		}

		return cl;
	}

}
