package net.mcshockwave.bbane;

import net.mcshockwave.MCS.Utils.LocUtils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public enum CenterEvent {

	bb_center0(
		"Chickens + Gravel"),
	bb_center1(
		"Cows + Sugar Cane"),
	bb_center2(
		"Spiders + Cobwebs"),
	bb_center3(
		"Iron + Coal"),
	bb_center4(
		"Diamond + Lapis"),
	bb_center5(
		"a Giant");

	public String	name;

	CenterEvent(String name) {
		this.name = name;
	}

	public String getSchematic() {
		return name();
	}

	public void onStart() {
		Location center = new Location(BattleBane.wor(), 0, BattleBane.centerOrigin, 0);

		BattleBane.loadSchematic(getSchematic(), center);

		for (Entity e : BattleBane.wor().getEntities()) {
			if (!DefaultListener.canBuildCenter(e.getLocation().getBlock()) && !(e instanceof Player)) {
				e.remove();
			}
		}

		center.add(0, 2, 0);

		Random rand = new Random();
		if (this == bb_center0) {
			for (int i = 0; i < rand.nextInt(5) + 8; i++) {
				Location r = LocUtils.addRand(center.clone(), 10, 0, 10);
				r.getWorld().spawnEntity(r, EntityType.CHICKEN);
			}
		}
		if (this == bb_center1) {
			for (int i = 0; i < rand.nextInt(5) + 8; i++) {
				Location r = LocUtils.addRand(center.clone(), 10, 0, 10);
				r.getWorld().spawnEntity(r, EntityType.COW);
			}
		}
		if (this == bb_center2) {
			for (int i = 0; i < rand.nextInt(5) + 8; i++) {
				Location r = LocUtils.addRand(center.clone(), 10, 0, 10);
				Spider s = (Spider) r.getWorld().spawnEntity(r, EntityType.SPIDER);
				s.setRemoveWhenFarAway(false);
			}
		}
		if (this == bb_center5) {
			Giant g = (Giant) center.getWorld().spawnEntity(center, EntityType.GIANT);
			g.setMaxHealth(200);
			g.setHealth(g.getMaxHealth());
			g.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
			g.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		}
	}

}
