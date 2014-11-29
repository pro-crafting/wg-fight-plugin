package de.pro_crafting.wg.group;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.pro_crafting.wg.OfflineRunable;
import de.pro_crafting.wg.Util;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;

public class GroupManager {
	private WarGear plugin;
	private Arena arena;
	private Group group1;
	private Group group2;
	private Map<PlayerRole, PlayerGroupKey> groupKeys;
	
	public GroupManager(WarGear plugin, Arena arena) {
		this.plugin = plugin;
		this.group1 = new Group(PlayerRole.Team1);
		this.group2 = new Group(PlayerRole.Team2);
		this.arena = arena;
		
		this.groupKeys = new HashMap<PlayerRole, PlayerGroupKey>();
		this.groupKeys.put(PlayerRole.Team1, new PlayerGroupKey(arena, PlayerRole.Team1));
		this.groupKeys.put(PlayerRole.Team2, new PlayerGroupKey(arena, PlayerRole.Team2));
		this.groupKeys.put(PlayerRole.Viewer, new PlayerGroupKey(arena, PlayerRole.Viewer));
	}
	
	public Location getGroupSpawn(PlayerRole role) {
		if (role == PlayerRole.Team1) {
			return arena.getRepo().getTeam1Warp();
		} else {
			return arena.getRepo().getTeam2Warp();
		}
	}
	
	public void quitFight() {
		quiteFightFoGroup(this.group1);
		quiteFightFoGroup(this.group2);
		this.group1 = new Group(PlayerRole.Team1);
		this.group2 = new Group(PlayerRole.Team2);
	}
	
	private void quiteFightFoGroup(Group group) {
		final Location teleportLocation = this.arena.getRepo().getSpawnWarp();
		OfflineRunable fightQuiter = new OfflineRunable() {
			
			public void run(GroupMember member) {
				member.getPlayer().getInventory().clear();
				member.getPlayer().teleport(teleportLocation, TeleportCause.PLUGIN);
			}
		};
		this.plugin.getOfflineManager().run(fightQuiter, group);
	}
	
	public void sendWinnerOutput(PlayerRole role) {
		String group = getRolePrefix(role)+"§2"+concateGroupPlayers(this.getTeamOfGroup(role));
		this.arena.broadcastMessage(group + " hat gewonnen!");
	}
	
	private String concateGroupPlayers(Group group) {
		String ret = "";
		for (GroupMember member : group.getMembers()) {
			if(member.isOnline()){
				ret += member.getPlayer().getDisplayName()+ " ";
			} else {
				ret += member.getOfflinePlayer().getName()+ " ";
			}
			
		}
		return ret.trim();
	}
	
	public void sendGroupOutput() {
		String group1 = arena.getRepo().getTeam1Prefix()+
				getRolePrefix(PlayerRole.Team1)+ ChatColor.YELLOW +""+ ChatColor.ITALIC+concateGroupPlayers(this.getGroup1());
		String group2 = arena.getRepo().getTeam2Prefix()+
				getRolePrefix(PlayerRole.Team2)+ ChatColor.YELLOW +""+ ChatColor.ITALIC+concateGroupPlayers(this.getGroup2());
		this.arena.broadcastMessage(ChatColor.YELLOW +""+ ChatColor.ITALIC+group1);
		this.arena.broadcastMessage(ChatColor.YELLOW +""+ ChatColor.ITALIC+group2);
	}
	
	private String getRolePrefix(PlayerRole role) {
		return "§8["+getPrefix(role)+role.toString()+"§8]";
	}
	
	public void healGroup(Group group) {
		OfflineRunable healer = new OfflineRunable() {
			public void run(GroupMember member) {
				Util.makeHealthy(member.getPlayer());
			}
		};
		this.plugin.getOfflineManager().run(healer, group);
	}
	 
	public Group getTeamOfGroup(PlayerRole role) {
		if (role == PlayerRole.Team1) {
			return this.group1;
		} else {
			return this.group2;
		}
	}
	
	public String getPrefix(PlayerRole role) {
		if (role == PlayerRole.Team1) {
			return this.arena.getRepo().getTeam1Prefix();
		} else if (role == PlayerRole.Team2) {
			return this.arena.getRepo().getTeam2Prefix();
		}
		return "§7";
	}
	
	public PlayerRole getRole(OfflinePlayer player) {
		Group group = getGroupOfPlayer(player);
		if (group != null) {
			return group.getRole();
		}
		return PlayerRole.Viewer;
	}
	
	public Group getGroupOfPlayer(OfflinePlayer p) {
		if (this.group1.getMember(p) != null) {
			return this.group1;
		} else if (this.group2.getMember(p) != null) {
			return this.group2;
		}
		return null;
	}
	
	public Group getGroupWithOutLeader() {
		if (!this.group1.hasLeader()) {
			return this.group1;
		} else if (!this.group2.hasLeader()) {
			return this.group2;
		}
		return null;
	}
	 
	public boolean isReady() {
		return this.group1.isReady() && this.group2.isReady();
	}
	 
	public boolean isAlive(Player p) {
		if (this.getGroupOfPlayer(p) == null || this.getGroupOfPlayer(p).getMember(p) == null) {
			 return false;
		}
		return this.getGroupOfPlayer(p).getMember(p).isAlive();
	}
	 
	public GroupMember getGroupMember(OfflinePlayer p) {
		if (this.group1.getMember(p) != null) {
			return this.group1.getMember(p);
		}
		if (this.group2.getMember(p) != null) {
			return this.group2.getMember(p);
		}
		return null;
	}
	
	public Group getGroup1() {
		return group1;
	}

	public Group getGroup2() {
		return group2;
	}
	
	public PlayerGroupKey getGroupKey(OfflinePlayer player) {
		return this.groupKeys.get(this.getRole(player));
	}
}
