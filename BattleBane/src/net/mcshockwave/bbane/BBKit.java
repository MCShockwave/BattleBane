package net.mcshockwave.bbane;

import org.bukkit.Material;
import org.bukkit.entity.Player;
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
		new ItemStack(Material.TNT, 3));

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

	public void onUse(Player p) {
		used.remove(p.getName());
		used.put(p.getName(), this);
	}

	public void giveKit(Player p) {
		p.getInventory().addItem(kit);

		if (this == Civilian) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
		}
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

}
