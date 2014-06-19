package de.hrc_gaming.wg.arena.ui;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import de.hrc_gaming.wg.WarGear;
import de.hrc_gaming.wg.arena.Arena;
import de.hrc_gaming.wg.arena.State;
import de.hrc_gaming.wg.event.ArenaStateChangedEvent;
import de.hrc_gaming.wg.team.TeamMember;
import de.hrc_gaming.wg.team.TeamNames;

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
	private OfflinePlayer timePlayer;
	
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
		board.registerNewObjective("Lebensanzeige", "dummy");
		board.getObjective("Lebensanzeige").setDisplaySlot(DisplaySlot.SIDEBAR);
		initTeams();
		timePlayer = this.plugin.getServer().getOfflinePlayer(ChatColor.GREEN+"Zeit (m):");
		board.getObjective("Lebensanzeige").getScore(timePlayer).setScore(this.arena.getRepo().getScoreboardTime());
	}
	
	private void initTeams()
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		teamRed = createTeam("team_red", "Team Red", ChatColor.RED.toString()+"(T)");
		teamLeaderRed = createTeam("team_red_leader", "Teamleader Red", ChatColor.RED.toString()+"(C)");
		
		teamBlue = createTeam("team_blue", "Team Blue", ChatColor.BLUE.toString()+"(T)");
		teamLeaderBlue = createTeam("team_blue_leader", "Teamleader Blue", ChatColor.BLUE.toString()+"(C)");
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
		if (team == TeamNames.Team1)
		{
			removeMemberFromTeam(teamLeaderRed, teamRed, member);
		}
		else if (team == TeamNames.Team2)
		{
			removeMemberFromTeam(teamLeaderBlue, teamBlue, member);
		}
		board.resetScores(member.getOfflinePlayer());
	}
	
	private void removeMemberFromTeam(Team leader, Team memberTeam, TeamMember member)
	{
		if (member.isTeamLeader())
		{
			leader.removePlayer(member.getOfflinePlayer());
		}
		else
		{
			memberTeam.removePlayer(member.getOfflinePlayer());
		}
	}
	
	public void addTeamMember(TeamMember member, TeamNames team)
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		initScoreboard();
		if (team == TeamNames.Team1)
		{
			addMemberToTeam(teamLeaderRed, teamRed, member);
		}
		else if (team == TeamNames.Team2)
		{
			addMemberToTeam(teamLeaderBlue, teamBlue, member);
		}
		board.getObjective("Lebensanzeige").getScore(member.getPlayer()).setScore((int)member.getPlayer().getHealth());
	}
	
	private void addMemberToTeam(Team leader, Team memberTeam, TeamMember member)
	{
		if (member.isTeamLeader())
		{
			leader.addPlayer(member.getOfflinePlayer());
		}
		else
		{
			memberTeam.addPlayer(member.getOfflinePlayer());
		}
	}
	
	public void clearScoreboard()
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		teamRed.unregister();
		teamBlue.unregister();
		teamLeaderBlue.unregister();
		teamLeaderRed.unregister();
		board.getObjective("Lebensanzeige").unregister();
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
			board.getObjective("Lebensanzeige").getScore(p).setScore((int)Math.ceil(p.getHealth()));
		}
		else
		{
			board.resetScores(p);
		}
	}
	
	public void updateTime(int time)
	{
		board.getObjective("Lebensanzeige").getScore(timePlayer).setScore(time);
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
		else if (event.getFrom() == State.Running)
		{
			if (this.timer.getIsRunning())
			{
				this.timer.stop();
				clearScoreboard();
			}
		}
	}
}
