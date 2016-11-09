package de.pro_crafting.wg.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import de.pro_crafting.common.scoreboard.Criteria;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.event.ArenaStateChangeEvent;
import de.pro_crafting.wg.group.Group;
import de.pro_crafting.wg.group.GroupManager;
import de.pro_crafting.wg.group.GroupMember;
import de.pro_crafting.wg.group.PlayerGroupKey;
import de.pro_crafting.wg.group.PlayerRole;

public class ScoreboardDisplay implements Listener{
	private WarGear plugin;	
	
	private final String healthName = "§bLeben";
	private final String infoName = "§bInfo";
	private final String belowNameHealthName = "/ 20";
	
	private final String teamRed = "team_red";
	private final String teamLeaderRed = "team_red_leader";
	private final String teamBlue = "team_blue";
	private final String teamLeaderBlue = "team_blue_leader";
	
	private final String timeName = ChatColor.GREEN+"Zeit (m):";
	private final String team1CannonName = ChatColor.DARK_GREEN+"Kanonen";
	private final String team2CannonName = ChatColor.AQUA+"Kanonen";
	
	private Map<Arena, BukkitTask> timers;
	private BukkitTask objectiveSwitcher;
	
	public ScoreboardDisplay(WarGear plugin) {
		this.plugin = plugin;
		this.timers = new HashMap<Arena, BukkitTask>();
		Bukkit.getPluginManager().registerEvents(this, this.plugin);
		this.objectiveSwitcher = Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable() {
			private boolean info;
			public void run() {
				String toShow = healthName;
				if (info) {
					toShow = infoName;
				}
				for (Arena arena : ScoreboardDisplay.this.plugin.getArenaManager().getArenas()) {
					switchScoreboardObjective(arena, toShow);
				}
				info = !info;
			}
		}, 0, 5*20);
	}
	
	private void switchScoreboardObjective(Arena arena, String toShow) {
		GroupManager groupManager = arena.getGroupManager();
		ScoreboardDisplay.this.plugin.getScoreboardManager().showObjective(groupManager.getGroupKey(PlayerRole.Team1), toShow, DisplaySlot.SIDEBAR);
		ScoreboardDisplay.this.plugin.getScoreboardManager().showObjective(groupManager.getGroupKey(PlayerRole.Team2), toShow, DisplaySlot.SIDEBAR);
		ScoreboardDisplay.this.plugin.getScoreboardManager().showObjective(groupManager.getGroupKey(PlayerRole.Viewer), toShow, DisplaySlot.SIDEBAR);
	
	}
	
	private void initScoreboard(Arena arena) {
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		init(arena.getGroupManager().getGroupKey(PlayerRole.Team1));
		init(arena.getGroupManager().getGroupKey(PlayerRole.Team2));
		init(arena.getGroupManager().getGroupKey(PlayerRole.Viewer));
		setScore(arena, timeName, arena.getRepo().getScoreboardTime(), infoName);
	}
	
	private void init(PlayerGroupKey groupKey) {
		Scoreboard board = this.plugin.getScoreboardManager().getScoreboard(groupKey);
		if (board.getObjective(DisplaySlot.SIDEBAR) != null) {
			return;
		}
		this.plugin.getScoreboardManager().createObjective(groupKey, healthName, DisplaySlot.SIDEBAR, Criteria.Dummy);
		this.plugin.getScoreboardManager().createObjective(groupKey, infoName, DisplaySlot.SIDEBAR, Criteria.Dummy);
		this.plugin.getScoreboardManager().createObjective(groupKey, belowNameHealthName, DisplaySlot.BELOW_NAME, Criteria.Dummy);
		
		initTeams(groupKey.getArena(), board);
	}
	
	private void initTeams(Arena arena, Scoreboard board) {
		createTeam(this.teamRed, "Team Red", arena.getRepo().getTeam1Prefix()+"(T)", board);
		createTeam(this.teamLeaderRed, "Teamleader Red", arena.getRepo().getTeam1Prefix()+"(C)", board);
		
		createTeam(this.teamBlue, "Team Blue", arena.getRepo().getTeam2Prefix()+"(T)", board);
		createTeam(this.teamLeaderBlue, "Teamleader Blue",arena.getRepo().getTeam2Prefix()+"(C)", board);
	}
	
	private Team createTeam(String teamName, String displayName, String prefix, Scoreboard board) {
		Team created = board.registerNewTeam(teamName);
		created.setDisplayName(displayName);
		created.setPrefix(prefix);
		created.setCanSeeFriendlyInvisibles(false);
		return created;
	}
	
	private Team getTeamMemberRed(PlayerGroupKey key) {
		return this.plugin.getScoreboardManager().getScoreboard(key).getTeam(teamRed);
	}
	
	private Team getTeamLeaderRed(PlayerGroupKey key) {
		return this.plugin.getScoreboardManager().getScoreboard(key).getTeam(teamLeaderRed);
	}
	
	private Team getTeamMemberBlue(PlayerGroupKey key) {
		return this.plugin.getScoreboardManager().getScoreboard(key).getTeam(teamBlue);
	}

	private Team getTeamLeaderBlue(PlayerGroupKey key) {
		return this.plugin.getScoreboardManager().getScoreboard(key).getTeam(teamLeaderBlue);
	}
	
	private Team getTeamMember(PlayerGroupKey groupKey, PlayerRole role) {
		if (role == PlayerRole.Team1) {
			return this.getTeamMemberRed(groupKey);
		} else {
			return this.getTeamMemberBlue(groupKey);
		}
	}
	
	private Team getTeamLeader(PlayerGroupKey groupKey, PlayerRole role) {
		if (role == PlayerRole.Team1) {
			return this.getTeamLeaderRed(groupKey);
		} else {
			return this.getTeamLeaderBlue(groupKey);
		}
	}
	
	private Team getTeam(PlayerGroupKey key, PlayerRole role, boolean isTeamLeader) {
		if (isTeamLeader) {
			return this.getTeamLeader(key, role);
		} else {
			return this.getTeamMember(key, role);
		}
	}
	
	public void addViewer(PlayerGroupKey key, Player p) {
		if (!key.getArena().getRepo().isScoreboardEnabled()) {
			return;
		}
		p.setScoreboard(this.plugin.getScoreboardManager().getScoreboard(key));
	}
	
	public void removeViewer(Arena arena, Player p) {
		if (!arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}
	
	public void removeTeamMember(Arena arena, GroupMember member, PlayerRole role) {
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		initScoreboard(arena);
		OfflinePlayer player = member.getOfflinePlayer();
		removeMemberFromTeam(arena, player, member.isLeader(), role);
		removeScore(arena, player.getName());
		if (player.isOnline()) {
			Player oPlayer = (Player)player;
			if(this.isNicked(oPlayer)){
				removeScore(arena, oPlayer.getPlayerListName());
				oPlayer.setPlayerListName(oPlayer.getDisplayName());
			}
			Arena at = this.plugin.getArenaManager().getArenaAt(oPlayer.getLocation());
			if (at == null) {
				this.removeViewer(arena, oPlayer);
			} else {
				this.addViewer(arena.getGroupManager().getGroupKey(PlayerRole.Viewer), oPlayer);
			}
		}
	}
	
	private void removeMemberFromTeam(Arena arena, OfflinePlayer player, boolean isTeamLeader, PlayerRole role) {
		this.getTeam(arena.getGroupManager().getGroupKey(PlayerRole.Team1), role, isTeamLeader).removePlayer(player);
		this.getTeam(arena.getGroupManager().getGroupKey(PlayerRole.Team2), role, isTeamLeader).removePlayer(player);
		this.getTeam(arena.getGroupManager().getGroupKey(PlayerRole.Viewer), role, isTeamLeader).removePlayer(player);
	}
	
	public void addTeamMember(Arena arena, GroupMember member, PlayerRole role) {
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		initScoreboard(arena);
		Player player = member.getPlayer();
		addMemberToTeam(arena, player, member.isLeader(), role);
		if(this.isNicked(player)){
			setNickedPlayerListName(arena, member, role);
			setScore(arena, player.getPlayerListName(), (int)Math.ceil(player.getHealth()), this.healthName);
		} else {
			setScore(arena, player.getName(), (int)Math.ceil(player.getHealth()), this.healthName);
		}
		
		setScore(arena, player.getName(), (int)Math.ceil(player.getHealth()), belowNameHealthName);
		
		if (arena.contains(player.getLocation())) {
			addViewer(arena.getGroupManager().getGroupKey(role), player);
		}
	}
	
	private boolean isNicked(Player player){
		return !player.getName().equals(player.getDisplayName());
	}
	
	private void setNickedPlayerListName(Arena arena, GroupMember member, PlayerRole role){
		GroupManager groupmanager = arena.getGroupManager();
		Player player = member.getPlayer();
		String prefix = "(T)";
		if(member.isLeader()){
			prefix = "(C)";
		}
		
		String playerlistname = groupmanager.getPrefix(PlayerRole.Team1) + prefix + player.getDisplayName();
		
		if(role == PlayerRole.Team2){
			playerlistname = playerlistname.replaceFirst(groupmanager.getPrefix(PlayerRole.Team1), groupmanager.getPrefix(PlayerRole.Team2));
		}
		
		
		if(playerlistname.length() > 16){
			playerlistname = playerlistname.substring(0, 16);
		}
		
		player.setPlayerListName(playerlistname);
	}
	
	private void addMemberToTeam(Arena arena, OfflinePlayer player, boolean isTeamLeader, PlayerRole role) {
		this.getTeam(arena.getGroupManager().getGroupKey(PlayerRole.Team1), role, isTeamLeader).addPlayer(player);
		this.getTeam(arena.getGroupManager().getGroupKey(PlayerRole.Team2), role, isTeamLeader).addPlayer(player);
		this.getTeam(arena.getGroupManager().getGroupKey(PlayerRole.Viewer), role, isTeamLeader).addPlayer(player);
	}
	
	public void clearScoreboard(Arena arena) {
		GroupManager groupManager = arena.getGroupManager();
		this.plugin.getScoreboardManager().clearScoreboard(groupManager.getGroupKey(PlayerRole.Team1));
		this.plugin.getScoreboardManager().clearScoreboard(groupManager.getGroupKey(PlayerRole.Team2));
		this.plugin.getScoreboardManager().clearScoreboard(groupManager.getGroupKey(PlayerRole.Viewer));
	}
	
	public void updateHealthOfPlayer(Arena arena, Player player) {
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		String name = player.getDisplayName();
		if (arena.getGroupManager().isAlive(player) && arena.getState() != State.Spectate) {
			int health = (int)Math.ceil(player.getHealth());
			setScore(arena, player.getPlayerListName(), health, this.healthName);
			setScore(arena, name, health, this.belowNameHealthName);
		}
		else {
			removeScore(arena, name);
		}
	}
	
	public void updateCannons(Arena arena, PlayerRole role, int count) {
		if (arena.getState() != State.Running) {
			return;
		}
		GroupManager groupManager = arena.getGroupManager();
		if (role == PlayerRole.Team1) {
			this.plugin.getScoreboardManager().setScore(groupManager.getGroupKey(PlayerRole.Viewer), team1CannonName, count, infoName);
			this.plugin.getScoreboardManager().setScore(groupManager.getGroupKey(PlayerRole.Team1), team1CannonName, count, infoName);
		} else {
			this.plugin.getScoreboardManager().setScore(groupManager.getGroupKey(PlayerRole.Viewer), team2CannonName, count, infoName);
			this.plugin.getScoreboardManager().setScore(groupManager.getGroupKey(PlayerRole.Team2), team2CannonName, count, infoName);
		}
	}
	
	public void updateTime(Arena arena, int time) {
		setScore(arena, timeName, time, this.infoName);
	}
	
	void stopTimer(Arena arena) {
		BukkitTask task = timers.get(arena);
		if (task != null) {
			task.cancel();
		}
	}
	
	private void setScore(Arena arena, String scoreName, int score, String objectiveName) {
		GroupManager groupManager = arena.getGroupManager();
		this.plugin.getScoreboardManager().setScore(groupManager.getGroupKey(PlayerRole.Team1), scoreName, score, objectiveName);
		this.plugin.getScoreboardManager().setScore(groupManager.getGroupKey(PlayerRole.Team2), scoreName, score, objectiveName);
		this.plugin.getScoreboardManager().setScore(groupManager.getGroupKey(PlayerRole.Viewer), scoreName, score, objectiveName);
	}
	
	private void removeScore(Arena arena, String scoreName) {
		GroupManager groupManager = arena.getGroupManager();
		this.plugin.getScoreboardManager().removeScore(groupManager.getGroupKey(PlayerRole.Team1), scoreName);
		this.plugin.getScoreboardManager().removeScore(groupManager.getGroupKey(PlayerRole.Team2), scoreName);
		this.plugin.getScoreboardManager().removeScore(groupManager.getGroupKey(PlayerRole.Viewer), scoreName);
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void arenaStateChangedHandler(ArenaStateChangeEvent event)
	{
		Arena arena = event.getArena();
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		if (event.getTo() == State.Setup) {
			for (UUID playerId : arena.getPlayers()) {
				Player player = this.plugin.getServer().getPlayer(playerId);
				if (player != null) {
					this.addViewer(arena.getGroupManager().getGroupKey(player), player);
				}
			}
			clearScoreboard(arena);
			initScoreboard(arena);
		}
		else if (event.getTo() == State.PreRunning) {
			for (UUID playerId : arena.getPlayers()) {
				Player player = this.plugin.getServer().getPlayer(playerId);
				if (player != null) {
					this.addViewer(arena.getGroupManager().getGroupKey(player), player);
				}
			}
		}
		else if (event.getTo() == State.Running) {
			BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, (Runnable)new ArenaTimerRunnable(this.plugin, arena), 0, 20*60);
			timers.put(arena, task);
		}
		else if (event.getTo() == State.Spectate) {
			stopTimer(arena);
			clearScoreboard(arena);
			removeNicked(arena.getGroupManager().getGroup1());
			removeNicked(arena.getGroupManager().getGroup2());
		}
	}
	
	private void removeNicked(Group team) {
		for (GroupMember member : team.getMembers()) {
			if (member.isOnline()) {
				Player p = member.getPlayer();
				if (this.isNicked(p)) {
					p.setPlayerListName(p.getDisplayName());
				}
			}
		}
	}
}
