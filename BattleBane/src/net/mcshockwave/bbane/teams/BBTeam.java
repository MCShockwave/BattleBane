package net.mcshockwave.bbane.teams;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.bbane.BBKit;
import net.mcshockwave.bbane.BattleBane;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public enum BBTeam {

	Red(
		500,
		3,
		500,
		ChatColor.RED,
		14),
	Blue(
		500,
		2,
		-500,
		ChatColor.AQUA,
		3),
	Yellow(
		-500,
		3,
		500,
		ChatColor.YELLOW,
		4),
	Green(
		-500,
		2,
		-500,
		ChatColor.GREEN,
		5);

	public Score		yorig;

	public int			x;
	public int			yoff;
	public int			z;
	public ChatColor	c;
	public short		data;

	public Score		points;

	public Team			team;

	@SuppressWarnings("deprecation")
	private BBTeam(int x, int yoffset, int z, ChatColor c, int data) {
		this.x = x;
		this.z = z;
		this.c = c;

		this.data = (short) data;

		this.yoff = yoffset;

		setOrigin(yoffset);

		Team t = BattleBane.score.getTeam(name());
		if (t != null) {
			t.unregister();
		}

		Objective ob = BattleBane.score.getObjective("Points");
		if (ob == null) {
			ob = BattleBane.score.registerNewObjective("Points", "dummy");
		}
		ob.setDisplayName("§6Battle Bane §7Points");
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);

		points = ob.getScore(Bukkit.getOfflinePlayer(c + name()));
		points.setScore(1);
		points.setScore(0);

		team = BattleBane.score.registerNewTeam(name());
		team.setPrefix(c.toString());
		team.setSuffix("§r");
		team.setAllowFriendlyFire(false);
		team.setCanSeeFriendlyInvisibles(true);
	}

	@SuppressWarnings("deprecation")
	public void setOrigin(int yoffset) {
		Objective ob = BattleBane.score.getObjective("Origin");
		if (ob == null) {
			ob = BattleBane.score.registerNewObjective("Origin", "dummy");
		}

		yorig = ob.getScore(Bukkit.getOfflinePlayer(name()));
		if (yorig.getScore() == 0) {
			yorig.setScore(BattleBane.wor().getHighestBlockYAt(x, z) + yoffset);
		}
	}

	public Location getSpawn() {
		return getSchemOrigin().add(3.5, 1, 0.5);
	}

	public Location getSchemOrigin() {
		return new Location(BattleBane.wor(), x, yorig.getScore(), z);
	}

	public Location getThroneRoom() {
		return getSchemOrigin().add(0.5, 5, 0.5);
	}

	public List<Player> getOnline() {
		ArrayList<Player> ret = new ArrayList<>();

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (isTeam(p)) {
				ret.add(p);
			}
		}

		return ret;
	}

	public void addPlayer(Player p) {
		team.addPlayer(p);
		getSpawn().getChunk().load();
		p.teleport(getSpawn());

		if (BBKit.getClassFor(p) != null) {
			BBKit.getClassFor(p).giveKit(p);
		}
	}

	public static void removePlayer(Player p) {
		Team pt = BattleBane.score.getPlayerTeam(p);
		if (pt != null) {
			pt.removePlayer(p);
		}
		BattleBane.lob().getSpawnLocation().getChunk().load();
		p.teleport(BattleBane.lob().getSpawnLocation());
	}

	public static BBTeam getTeamFor(OfflinePlayer p) {
		for (BBTeam t : values()) {
			if (t.team.hasPlayer(p)) {
				return t;
			}
		}
		return null;
	}

	public boolean isTeam(OfflinePlayer p) {
		try {
			if (getTeamFor(p) == null) {
				return false;
			}
			return getTeamFor(p) == this;
		} catch (Exception e) {
			return false;
		}
	}

	public static ItemMenu getTeamMenu(Player p) {
		ItemMenu cl = new ItemMenu("Team Selection", BBTeam.values().length);

		for (int i = 0; i < BBTeam.values().length; i++) {
			final BBTeam t = BBTeam.values()[i];
			Button b = new Button(true, Material.WOOL, 1, t.data, t.c + t.name(), "", "Click to join", t.team
					.getPlayers().size() + " players", "§a" + t.getOnline().size() + " online");
			b.setOnClick(new ButtonRunnable() {
				public void run(Player p, InventoryClickEvent event) {
					int players = t.team.getPlayers().size();
					for (BBTeam bbt : BBTeam.values()) {
						if (players >= bbt.team.getSize() + 2) {
							MCShockwave.send(t.c, p, "%s team is %s!", t.name(), "full");
							return;
						}
					}
					MCShockwave.send(t.c, p, "Joined team %s", t.name());
					t.addPlayer(p);
				}
			});

			cl.addButton(b, i);
		}

		return cl;
	}

}
