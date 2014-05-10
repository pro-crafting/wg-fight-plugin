package de.hrc_gaming.wg.arena.ui;

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
	private Team teamBlue;
	private ArenaTimer timer;
	
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
		OfflinePlayer timePlayer = this.plugin.getServer().getOfflinePlayer(ChatColor.GREEN+"Zeit (m):");
		board.getObjective("Lebensanzeige").getScore(timePlayer).setScore(this.arena.getRepo().getScoreboardTime());
	}
	
	private void initTeams()
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		teamRed = board.registerNewTeam("team_red");
		teamRed.setDisplayName("teamred");
		teamRed.setPrefix(ChatColor.RED+"");
		
		teamBlue = board.registerNewTeam("team_blue");
		teamBlue.setDisplayName("teamblue");
		teamBlue.setPrefix(ChatColor.BLUE+"");
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
			teamRed.removePlayer(member.getPlayer());
		}
		else if (team == TeamNames.Team2)
		{
			teamBlue.removePlayer(member.getPlayer());
		}
		board.resetScores(member.getPlayer());
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
			teamRed.addPlayer(member.getPlayer());
		}
		else if (team == TeamNames.Team2)
		{
			teamBlue.addPlayer(member.getPlayer());
		}
		board.getObjective("Lebensanzeige").getScore(member.getPlayer()).setScore(20);
	}
	
	private void clearScoreboard()
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		teamRed.unregister();
		teamBlue.unregister();
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
		p.setScoreboard(manager.getNewScoreboard());
	}
	
	public void updateHealthOfPlayer(Player p)
	{
		if (!this.arena.getRepo().isScoreboardEnabled())
		{
			return;
		}
		if (this.arena.getTeam().isPlayerAlive(p))
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
		board.getObjective("Lebensanzeige").getScore(this.plugin.getServer().getOfflinePlayer(ChatColor.GREEN+"Zeit (m):")).setScore(time);
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
