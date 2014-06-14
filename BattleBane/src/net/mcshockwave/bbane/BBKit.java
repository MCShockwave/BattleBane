package net.mcshockwave.bbane;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
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

import java.util.ArrayList;
import java.util.HashMap;

public enum BBKit {

	Civilian(
		Material.WORKBENCH,
		1,
		0,
		"Get speed II instead//of speed I!"),
	Miner(
		Material.IRON_PICKAXE,
		1,
		0,
		"Get 5 ores instead//of 4!",
		new ItemStack(Material.WOOD_PICKAXE)),
	Demoman(
		Material.TNT,
		3,
		0,
		"Get TNT that you//can place and it//instantly ignites!",
		new ItemStack(Material.TNT, 3)),
	Assassin(
		Material.COMPASS,
		1,
		0,
		"[Not Done Yet]",
		new ItemStack(Material.COMPASS)),
	Golem(
		Material.PUMPKIN,
		1,
		0,
		"[Not Done Yet]",
		new ItemStack(Material.SNOW_BALL, 5)),
	Medic(
		Material.POTION,
		1,
		8197,
		"When you kill a mob//or player, you split//health between your//teammates and yourself"),
	Bat(
		Material.FEATHER,
		1,
		0,
		"[Not Done Yet]"),
	Archer(
		Material.BOW,
		1,
		0,
		"Use a variety of modes//on your bow to change//the way you fire arrows!",
		new ItemStack(Material.BOW),
		new ItemStack(Material.ARROW, 30)),
	Pyro(
		Material.FLINT_AND_STEEL,
		1,
		0,
		"Start with a flint and//steel used to burn//enemies and only enemies./"
				+ "/People you set on fire//take more damage from you.",
		new ItemStack(Material.FLINT_AND_STEEL));

	protected static HashMap<String, BBKit>	used	= new HashMap<>();

	public Material							ico;
	public int								am, da;
	public String							name;

	public ItemStack[]						kit;

	public String[]							desc;

	private BBKit(Material ico, int am, int da, String description, ItemStack... items) {
		this.ico = ico;
		this.am = am;
		this.da = da;

		this.kit = items;

		this.name = name().replace('_', ' ');

		desc = description.split("//");
	}

	public boolean hasKit(String pl) {
		return isDone() && (this == Civilian || SQLTable.BattleBaneItems.getInt("Username", pl, name()) > 0);
	}

	public boolean isDone() {
		return !(desc.length > 0 && desc[0].equalsIgnoreCase("[Not Done Yet]"));
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
		return Civilian;
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
		p.getInventory().addItem(ItemMetaUtils.setLore(new ItemStack(Material.COOKED_BEEF, 12), "§6Kit Item"));

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

	public String[] getLore(Player p) {
		ArrayList<String> ret = new ArrayList<>();
		ret.add("");
		for (String s : desc) {
			ret.add("§e" + s);
		}
		ret.add("");
		if (hasKit(p.getName())) {
			ret.add("Click to use");
		} else if (isDone()) {
			ret.add("§cLocked!");
			ret.add("§cUnlock with points");
			ret.add("§c at buy.mcshockwave.net");
		} else {
			ret.add("§cThis class is not done");
			ret.add("§c being coded");
		}

		return ret.toArray(new String[0]);
	}

	public static ItemMenu getClassMenu(Player p, final boolean kill) {
		ItemMenu cl = new ItemMenu("Classes", BBKit.values().length);

		for (int i = 0; i < BBKit.values().length; i++) {
			final BBKit cs = BBKit.values()[i];
			if (cs.hasKit(p.getName())) {
				Button b = new Button(true, cs.ico, cs.am, cs.da, cs.name, cs.getLore(p));
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
			} else {
				Button b = new Button(true, Material.WOOL, 1, 14, cs.name, cs.getLore(p));
				b.setOnClick(new ButtonRunnable() {
					public void run(Player p, InventoryClickEvent event) {
						p.sendMessage("§cUnlock at: §4http://buy.mcshockwave.net");
					}
				});

				cl.addButton(b, i);
			}
		}

		return cl;
	}
}
