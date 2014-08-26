package de.pro_crafting.wg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.event.FightQuitEvent;
import de.pro_crafting.wg.event.WinQuitEvent;
import de.pro_crafting.wg.team.TeamMember;
import de.pro_crafting.wg.team.WgTeam;

public class OfflineManager implements Listener {
	private WarGear plugin;
	private Map<WgTeam, List<OfflineRunable>> teamRunnables;
	private Map<String, List<OfflineRunable>> memberRunnables;
	private List<TeamMember> offlineTeamMembers;
	private BukkitTask task;

	public OfflineManager(WarGear plugin) {
		this.plugin = plugin;
		this.teamRunnables = new HashMap<WgTeam, List<OfflineRunable>>();
		this.memberRunnables = new HashMap<String, List<OfflineRunable>>();
		this.offlineTeamMembers = new ArrayList<TeamMember>();
		this.task = this.plugin.getServer().getScheduler()
				.runTaskTimer(plugin, new Runnable() {
					public void run() {
						checkTeamMembers();
					}
				}, 0, 20);
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public boolean run(OfflineRunable runable, WgTeam team) {
		if (!team.isOnline()) {
			for (TeamMember member : team.getTeamMembers().values()) {
				run(runable, member);
			}
		} else {
			runTeam(runable, team);
		}
		return team.isOnline();
	}

	public boolean runComplete(OfflineRunable runable, WgTeam team) {
		if (!team.isOnline()) {
			if (!this.teamRunnables.containsKey(team)) {
				this.teamRunnables.put(team, new ArrayList<OfflineRunable>());
			}
			this.teamRunnables.get(team).add(runable);
		} else {
			runTeam(runable, team);
		}
		return team.isOnline();
	}

	public boolean run(OfflineRunable runable, TeamMember member) {
		if (!member.isOnline()) {
			if (!this.memberRunnables.containsKey(member)) {
				this.memberRunnables.put(member.getOfflinePlayer().getUniqueId().toString(),
						new ArrayList<OfflineRunable>());
			}
			this.memberRunnables.get(member.getOfflinePlayer().getUniqueId().toString()).add(runable);
		} else {
			runable.run(member);
		}
		return member.isOnline();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void handlePlayerQuits(PlayerQuitEvent event) {
		Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(
				event.getPlayer());
		if (arena != null) {
			TeamMember member = arena.getTeam()
					.getTeamMember(event.getPlayer());
			if (member.isAlive()) {
				this.offlineTeamMembers.add(member);
			}
		}
	}

	private boolean isTooLongOffline(TeamMember member) {
		return (System.currentTimeMillis() - member.getOfflinePlayer()
				.getLastPlayed()) > 30 * 1000;
	}

	private void checkTeamMembers() {
		Iterator<TeamMember> offlineIterator = this.offlineTeamMembers
				.iterator();
		while (offlineIterator.hasNext()) {
			TeamMember current = offlineIterator.next();
			if (current.isOnline()) {
				offlineIterator.remove();
			} else if (isTooLongOffline(current)) {
				offlineIterator.remove();
				killTeamMember(current);
			}
		}

		Iterator<Entry<String, List<OfflineRunable>>> memberIterator = this.memberRunnables
				.entrySet().iterator();
		while (memberIterator.hasNext()) {
			Entry<String, List<OfflineRunable>> current = memberIterator
					.next();
			TeamMember member = this.plugin.getArenaManager().getArenaOfTeamMember(Bukkit.getOfflinePlayer(UUID.fromString(current.getKey()))).getTeam().getTeamMember(Bukkit.getOfflinePlayer(UUID.fromString(current.getKey())));
			if (member.isOnline()) {
				for (OfflineRunable runable : current.getValue()) {
					runable.run(member);
				}
				memberIterator.remove();
			} else if (isTooLongOffline(member)) {
				memberIterator.remove();
				killTeamMember(member);
			}
		}

		Iterator<Entry<WgTeam, List<OfflineRunable>>> teamIterator = this.teamRunnables
				.entrySet().iterator();
		while (teamIterator.hasNext()) {
			Entry<WgTeam, List<OfflineRunable>> current = teamIterator.next();
			boolean everyoneOnline = true;
			for (TeamMember member : current.getKey().getTeamMembers().values()) {
				if (isTooLongOffline(member)) {
					killTeamMember(member);
					teamIterator.remove();
				} else if (!member.isOnline()) {
					everyoneOnline = false;
				}
			}
			if (everyoneOnline) {
				for (OfflineRunable runable : current.getValue()) {
					runTeam(runable, current.getKey());
				}
				teamIterator.remove();
			}
		}
	}

	private void killTeamMember(TeamMember member)
	{
		OfflinePlayer player = member.getOfflinePlayer();
		Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(player);
		if (arena == null) {
			return;
		}
		WgTeam team = arena.getTeam().getTeamOfPlayer(player);
		if (arena.getState() == State.Setup) {
			if (member.isTeamLeader()) {
				Iterator<Entry<UUID, TeamMember>> memberIterator = team.getTeamMembers().entrySet().iterator();
				while(memberIterator.hasNext()) {
					TeamMember toRemove = memberIterator.next().getValue();
					this.plugin.getScoreboard().removeTeamMember(arena, toRemove, team.getTeamName());
					memberIterator.remove();
				}
			} else {
				team.remove(player);
				this.plugin.getScoreboard().removeTeamMember(arena, member, team.getTeamName());
			}
		} else {
			this.plugin.getScoreboard().removeTeamMember(arena, member, team.getTeamName());
			member.setAlive(false);
		}
		if ((!team.isAlive() || !team.isOnline() || team.getTeamMembers().size() == 0) && (arena.getState() == State.PreRunning || arena.getState() == State.Running)) {
			FightQuitEvent event = new WinQuitEvent(arena, "Gegnerisches Team ist offline.", arena.getTeam().getTeamOfName(team.getTeamName() == PlayerRole.Team1 ? 
					PlayerRole.Team1 : PlayerRole.Team2), team, FightQuitReason.FightLeader);
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	private void runTeam(OfflineRunable runable, WgTeam team) {
		for (TeamMember member : team.getTeamMembers().values()) {
			runMember(runable, member);
		}
	}

	private void runMember(OfflineRunable runable, TeamMember member) {
		if (member.isOnline()) {
			runable.run(member);
		}
	}
}
