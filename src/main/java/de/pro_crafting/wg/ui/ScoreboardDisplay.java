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
import de.pro_crafting.wg.PlayerRole;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.event.ArenaStateChangeEvent;
import de.pro_crafting.wg.team.TeamMember;
import de.pro_crafting.wg.team.WgTeam;

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
				for (Arena arena : ScoreboardDisplay.this.plugin.getArenaManager().getArenas().values()) {
					ScoreboardDisplay.this.plugin.getScoreboardManager().showObjective(arena, toShow, DisplaySlot.SIDEBAR);
				}
				info = !info;
			}
		}, 0, 5*20);
	}
	
	private void initScoreboard(Arena arena) {
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		Scoreboard board = this.plugin.getScoreboardManager().getScoreboard(arena);
		if (board.getObjective(DisplaySlot.SIDEBAR) != null) {
			return;
		}
		this.plugin.getScoreboardManager().createObjective(arena, healthName, DisplaySlot.SIDEBAR, Criteria.Dummy);
		this.plugin.getScoreboardManager().createObjective(arena, infoName, DisplaySlot.SIDEBAR, Criteria.Dummy);
		this.plugin.getScoreboardManager().createObjective(arena, belowNameHealthName, DisplaySlot.BELOW_NAME, Criteria.Dummy);
		
		this.plugin.getScoreboardManager().setScore(arena, timeName, arena.getRepo().getScoreboardTime(), infoName);
		
		initTeams(arena, board);
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
	
	private Team getTeamRed(Arena arena) {
		return this.plugin.getScoreboardManager().getScoreboard(arena).getTeam(teamRed);
	}
	
	private Team getTeamLeaderRed(Arena arena) {
		return this.plugin.getScoreboardManager().getScoreboard(arena).getTeam(teamLeaderRed);
	}
	
	private Team getTeamBlue(Arena arena) {
		return this.plugin.getScoreboardManager().getScoreboard(arena).getTeam(teamBlue);
	}

	private Team getTeamLeaderBlue(Arena arena) {
		return this.plugin.getScoreboardManager().getScoreboard(arena).getTeam(teamLeaderBlue);
	}
	
	public void addViewer(Arena arena, Player p) {
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		p.setScoreboard(this.plugin.getScoreboardManager().getScoreboard(arena));
	}
	
	public void removeViewer(Arena arena, Player p) {
		if (!arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}
	
	public void removeTeamMember(Arena arena, TeamMember member, PlayerRole team) {
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		initScoreboard(arena);
		OfflinePlayer player = member.getOfflinePlayer();
		if(player.isOnline()){
			Player onlineplayer = (Player)player;
			if (team == PlayerRole.Team1) {
				removeMemberFromTeam(arena, this.getTeamLeaderRed(arena), this.getTeamRed(arena), onlineplayer, member.isTeamLeader());
			}
			else if (team == PlayerRole.Team2) {
				removeMemberFromTeam(arena, this.getTeamLeaderBlue(arena), this.getTeamBlue(arena), onlineplayer, member.isTeamLeader());
			}
			this.plugin.getScoreboardManager().removeScore(arena, onlineplayer.getPlayerListName());
			if (this.isNicked(onlineplayer)) {
				onlineplayer.setPlayerListName(onlineplayer.getDisplayName());
			}
		} else {
			if (team == PlayerRole.Team1) {
				removeMemberFromTeam(arena, this.getTeamLeaderRed(arena), this.getTeamRed(arena), player, member.isTeamLeader());
			}
			else if (team == PlayerRole.Team2) {
				removeMemberFromTeam(arena, this.getTeamLeaderBlue(arena), this.getTeamBlue(arena), player, member.isTeamLeader());
			}
			this.plugin.getScoreboardManager().removeScore(arena, player.getName());
		}
	}
	
	private void removeMemberFromTeam(Arena arena, Team leader, Team memberTeam, OfflinePlayer player, boolean isTeamLeader) {
		if (isTeamLeader) {
			leader.removePlayer(player);
		}
		else {
			memberTeam.removePlayer(player);
		}
	}
	
	public void addTeamMember(Arena arena, TeamMember member, PlayerRole team) {
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		initScoreboard(arena);
		Player player = member.getPlayer();
		if (team == PlayerRole.Team1) {
			addMemberToTeam(arena, this.getTeamLeaderRed(arena), this.getTeamRed(arena), player, member.isTeamLeader());
		}
		else if (team == PlayerRole.Team2) {
			addMemberToTeam(arena, this.getTeamLeaderBlue(arena), this.getTeamBlue(arena), player, member.isTeamLeader());
		}
		if(this.isNicked(player)){
			String nick = player.getDisplayName();
			if(nick.length() > 11){
				nick = nick.substring(0, 11);
			}
			if (team == PlayerRole.Team1) {
				if(member.isTeamLeader()){
					player.setPlayerListName(this.getTeamLeaderRed(arena).getPrefix() + nick);
				} else {
					player.setPlayerListName(this.getTeamRed(arena).getPrefix() + nick);
				}
				
			} else if (team == PlayerRole.Team2) {
				if(member.isTeamLeader()){
					player.setPlayerListName(this.getTeamLeaderBlue(arena).getPrefix() + nick);
				} else {
					player.setPlayerListName(this.getTeamRed(arena).getPrefix() + nick);
				}
			}
			this.plugin.getScoreboardManager().setScore(arena, player.getPlayerListName(), (int)Math.ceil(player.getHealth()), this.healthName);
			
		} else {
			this.plugin.getScoreboardManager().setScore(arena, player.getDisplayName(), (int)Math.ceil(player.getHealth()), this.healthName);
		}
		this.plugin.getScoreboardManager().setScore(arena, player.getDisplayName(), (int)Math.ceil(player.getHealth()), belowNameHealthName);
	}
	
	private boolean isNicked(Player player){
		return !player.getName().equals(player.getDisplayName());
	}
	
	private void addMemberToTeam(Arena arena, Team leader, Team memberTeam, Player player, boolean isTeamLeader) {
		if (isTeamLeader) {
			leader.addPlayer(player);
		}
		else {
			memberTeam.addPlayer(player);
		}
	}
	
	public void clearScoreboard(Arena arena) {
		this.plugin.getScoreboardManager().clearScoreboard(arena);
	}
	
	public void updateHealthOfPlayer(Arena arena, Player player) {
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		String name = player.getDisplayName();
		if (arena.getTeam().isAlive(player)) {
			int health = (int)Math.ceil(player.getHealth());
			this.plugin.getScoreboardManager().setScore(arena, player.getPlayerListName(), health, this.healthName);
			this.plugin.getScoreboardManager().setScore(arena, name, health, this.belowNameHealthName);
		}
		else {
			this.plugin.getScoreboardManager().removeScore(arena, name);
		}
	}
	
	public void updateTime(Arena arena, int time) {
		this.plugin.getScoreboardManager().setScore(arena, timeName, time, this.infoName);
	}
	
	void stopTimer(Arena arena) {
		BukkitTask task = timers.get(arena);
		if (task != null) {
			task.cancel();
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void arenaStateChangedHandler(ArenaStateChangeEvent event)
	{
		Arena arena = event.getArena();
		if (!arena.getRepo().isScoreboardEnabled()) {
			return;
		}
		if (event.getTo() == State.Setup) {
			clearScoreboard(arena);
			initScoreboard(arena);
		}
		else if (event.getTo() == State.PreRunning) {
			for (UUID playerId : arena.getPlayers()) {
				Player player = this.plugin.getServer().getPlayer(playerId);
				if (player != null) {
					this.addViewer(arena, player);
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
			removeNicked(arena.getTeam().getTeam1());
			removeNicked(arena.getTeam().getTeam2());
		}
	}
	
	private void removeNicked(WgTeam team) {
		for (TeamMember member : team.getTeamMembers().values()) {
			if (member.isOnline()) {
				Player p = member.getPlayer();
				if (this.isNicked(p)) {
					p.setPlayerListName(p.getDisplayName());
				}
			}
		}
	}
}
