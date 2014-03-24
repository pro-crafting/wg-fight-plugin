package me.Postremus.WarGear.Arena.ui;

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

import me.Postremus.WarGear.FightState;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Events.FightStateChangedEvent;
import me.Postremus.WarGear.Team.TeamMember;
import me.Postremus.WarGear.Team.TeamNames;
import me.Postremus.WarGear.Team.WgTeam;

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
		if (board.getObjective("Lebensanzeige") != null)
		{
			return;
		}
		board.registerNewObjective("Lebensanzeige", "dummy");
		board.getObjective("Lebensanzeige").setDisplaySlot(DisplaySlot.SIDEBAR);
		initTeams();
		board.getObjective("Lebensanzeige").getScore(this.plugin.getServer().getOfflinePlayer(ChatColor.GREEN+"Zeit (m):")).setScore(60);
	}
	
	private void initTeams()
	{
		teamRed = board.registerNewTeam("team_red");
		teamRed.setDisplayName("teamred");
		teamRed.setPrefix(ChatColor.RED+"");
		
		teamBlue = board.registerNewTeam("team_blue");
		teamBlue.setDisplayName("teamblue");
		teamBlue.setPrefix(ChatColor.BLUE+"");
	}
	
	public void removeTeamMember(TeamMember member, TeamNames team)
	{
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
		teamRed.unregister();
		teamBlue.unregister();
		board.getObjective("Lebensanzeige").unregister();
	}
	
	public void enterArena(Player p)
	{
		p.setScoreboard(board);
	}
	
	public void leaveArena(Player p)
	{
		p.setScoreboard(manager.getNewScoreboard());
	}
	
	public void updateHealthOfPlayer(Player p)
	{
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
	public void fightStateChangedHandler(FightStateChangedEvent event)
	{
		if (!event.getArena().equals(this.arena))
		{
			return;
		}
		if (event.getTo() == FightState.Setup)
		{
			initScoreboard();
		}
		else if (event.getTo() == FightState.Running)
		{
			this.timer.start();
		}
		else if (event.getFrom() == FightState.Running)
		{
			if (this.timer.getIsRunning())
			{
				this.timer.stop();
				clearScoreboard();
			}
		}
	}
}
