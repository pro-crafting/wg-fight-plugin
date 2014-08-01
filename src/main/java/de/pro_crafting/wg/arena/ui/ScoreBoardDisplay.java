package de.pro_crafting.wg.arena.ui;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.event.ArenaStateChangedEvent;
import de.pro_crafting.wg.team.TeamMember;
import de.pro_crafting.wg.team.TeamNames;

public class ScoreBoardDisplay implements Listener
{
	private WarGear plugin;
	private Arena arena;
	private ScoreboardManager manager;
	private Scoreboard board;
	private Team teamRed;
	private Team teamLeaderRed;
	private Team teamBlue;
	private Team teamLeaderBlue;
	private ArenaTimer timer;
	private Score timeScore;
	private Objective health;
	
	public ScoreBoardDisplay(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		this.arena = arena;
		manager = this.plugin.getServer().getScoreboardManager();
		board = manager.getNewScoreboard();
		timer = new ArenaTimer(this.plugin, this.arena);
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	private void initScoreboard()
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		if (board.getObjective("Lebensanzeige") != null)
		{
			return;
		}
		health = board.registerNewObjective("Lebensanzeige", "dummy");
		health.setDisplaySlot(DisplaySlot.SIDEBAR);
		initTeams();
		timeScore = health.getScore(ChatColor.GREEN+"Zeit (m):");
		timeScore.setScore(this.arena.getRepo().getScoreboardTime());
	}
	
	private void initTeams()
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		teamRed = createTeam("team_red", "Team Red", arena.getRepo().getTeam1Prefix()+"(T)");
		teamLeaderRed = createTeam("team_red_leader", "Teamleader Red", arena.getRepo().getTeam1Prefix()+"(C)");
		
		teamBlue = createTeam("team_blue", "Team Blue", arena.getRepo().getTeam2Prefix()+"(T)");
		teamLeaderBlue = createTeam("team_blue_leader", "Teamleader Blue",arena.getRepo().getTeam2Prefix()+"(C)");
	}
	
	private Team createTeam(String teamName, String displayName, String prefix)
	{
		Team created = board.registerNewTeam(teamName);
		created.setDisplayName(displayName);
		created.setPrefix(prefix);
		created.setCanSeeFriendlyInvisibles(false);
		return created;
	}
	
	public void removeTeamMember(TeamMember member, TeamNames team)
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		initScoreboard();
		OfflinePlayer player = member.getOfflinePlayer();
		if (team == TeamNames.Team1)
		{
			removeMemberFromTeam(teamLeaderRed, teamRed, player, member.isTeamLeader());
		}
		else if (team == TeamNames.Team2)
		{
			removeMemberFromTeam(teamLeaderBlue, teamBlue, player, member.isTeamLeader());
		}
		board.resetScores(player.getName());
	}
	
	private void removeMemberFromTeam(Team leader, Team memberTeam, OfflinePlayer player, boolean isTeamLeader)
	{
		if (isTeamLeader)
		{
			leader.removePlayer(player);
		}
		else
		{
			memberTeam.removePlayer(player);
		}
	}
	
	public void addTeamMember(TeamMember member, TeamNames team)
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		initScoreboard();
		Player player = member.getPlayer();
		if (team == TeamNames.Team1)
		{
			addMemberToTeam(teamLeaderRed, teamRed, player, member.isTeamLeader());
		}
		else if (team == TeamNames.Team2)
		{
			addMemberToTeam(teamLeaderBlue, teamBlue, player, member.isTeamLeader());
		}
		health.getScore(player.getName()).setScore((int)player.getHealth());
	}
	
	private void addMemberToTeam(Team leader, Team memberTeam, Player player, boolean isTeamLeader)
	{
		if (isTeamLeader)
		{
			leader.addPlayer(player);
		}
		else
		{
			memberTeam.addPlayer(player);
		}
	}
	
	public void clearScoreboard()
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		unregisterTeam(teamRed);
		unregisterTeam(teamBlue);
		unregisterTeam(teamLeaderBlue);
		unregisterTeam(teamLeaderRed);
		if (health != null)
		{
			health.unregister();
		}
	}
	
	private void unregisterTeam(Team team)
	{
		if (team != null)
		{
			team.unregister();
		}
	}
	
	public void addViewer(Player p)
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		p.setScoreboard(board);
	}
	
	public void removeViewer(Player p)
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		p.setScoreboard(manager.getMainScoreboard());
	}
	
	public void updateHealthOfPlayer(Player p)
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		if (this.arena.getTeam().isAlive(p))
		{
			health.getScore(p.getName()).setScore((int)Math.ceil(p.getHealth()));
		}
		else
		{
			board.resetScores(p.getName());
		}
	}
	
	public void updateTime(int time)
	{
		timeScore.setScore(time);
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void arenaStateChangedHandler(ArenaStateChangedEvent event)
	{
		if (!event.getArena().equals(this.arena))
		{
			return;
		}
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		if (event.getTo() == State.Setup)
		{
			initScoreboard();
		}
		else if (event.getTo() == State.PreRunning)
		{
			for (UUID playerId : this.arena.getPlayers())
			{
				Player player = this.plugin.getServer().getPlayer(playerId);
				if (player != null)
				{
					this.addViewer(player);
				}
			}
		}
		else if (event.getTo() == State.Running)
		{
			this.timer.start();
		}
		else if (event.getFrom() == State.Running || event.getFrom() == State.PreRunning)
		{
			if (this.timer.getIsRunning())
			{
				this.timer.stop();
			}
			clearScoreboard();
		}
	}
}
