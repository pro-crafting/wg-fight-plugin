package me.Postremus.WarGear.Arena.ui;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import me.Postremus.WarGear.FightState;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.TeamMember;
import me.Postremus.WarGear.Team.TeamNames;
import me.Postremus.WarGear.Team.WgTeam;

public class ScoreBoardDisplay 
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
	}
	
	private void initScoreboard()
	{
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
	
	public void updateHealthOfPlayer(Player p, int health)
	{
		TeamMember member = this.arena.getTeam().getTeamOfPlayer(p).getTeamMember(p);
		if (member.getAlive())
		{
			board.getObjective("Lebensanzeige").getScore(member.getPlayer()).setScore(health);
		}
		else
		{
			board.resetScores(member.getPlayer());
		}
	}
	
	public void updateTime(int time)
	{
		board.getObjective("Lebensanzeige").getScore(this.plugin.getServer().getOfflinePlayer(ChatColor.GREEN+"Zeit (m):")).setScore(time);
	}
	
	public void fightStateChanged()
	{
		if (this.arena.getFightState() == FightState.Setup)
		{
			if (board.getObjective("Lebensanzeige") == null)
			{
				initScoreboard();
			}
		}
		else if (this.arena.getFightState() == FightState.Running)
		{
			this.timer.start();
		}
		else if (this.arena.getFightState() == FightState.Idle)
		{
			if (this.timer.getIsRunning())
			{
				this.timer.stop();
				clearScoreboard();
			}
		}
	}
}
