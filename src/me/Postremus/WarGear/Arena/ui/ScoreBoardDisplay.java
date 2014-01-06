package me.Postremus.WarGear.Arena.ui;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
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

public class ScoreBoardDisplay 
{
	private WarGear plugin;
	private Arena arena;
	private ScoreboardManager manager;
	private Scoreboard board;
	private Team teamRed;
	private Team teamBlue;
	private int minutes;
	private int taskId;
	
	public ScoreBoardDisplay(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		this.arena = arena;
		manager = this.plugin.getServer().getScoreboardManager();
		board = manager.getNewScoreboard();
	}
	
	private void initScoreboard()
	{
		board.registerNewObjective("Lebensanzeige", "dummy");
		board.getObjective("Lebensanzeige").setDisplaySlot(DisplaySlot.SIDEBAR);
		initTeams();
	}
	
	private void initTeams()
	{
		teamRed = board.registerNewTeam("team_red");
		teamRed.setDisplayName("teamred");
		teamRed.setPrefix(ChatColor.RED+"");
		
		teamBlue = board.registerNewTeam("team_blue");
		teamBlue.setDisplayName("teamblue");
		teamBlue.setPrefix(ChatColor.BLUE+"");
		registerPlayers();
	}
	
	private void registerPlayers()
	{
		for (TeamMember player : this.arena.getTeam().getTeam1().getTeamMembers())
		{
			teamRed.addPlayer(player.getPlayer());
			board.getObjective("Lebensanzeige").getScore(player.getPlayer()).setScore(20);
		}
		for (TeamMember player : this.arena.getTeam().getTeam2().getTeamMembers())
		{
			teamBlue.addPlayer(player.getPlayer());
			board.getObjective("Lebensanzeige").getScore(player.getPlayer()).setScore(20);
		}
	}
	
	private void clearScoreboard()
	{
		teamRed.unregister();
		teamBlue.unregister();
		board.getObjective("Lebensanzeige").unregister();
	}
	
	public void update()
	{
		if (this.arena.getFightState() != FightState.Running)
		{
			return;
		}
		updateHealth();
	}
	
	private void minuteUpdater()
	{
		board.getObjective("Lebensanzeige").getScore(this.plugin.getServer().getOfflinePlayer(ChatColor.GREEN+"Dauer (m):")).setScore(minutes);
		minutes += 1;
	}
	
	public void enterArena(Player p)
	{
		p.setScoreboard(board);
	}
	
	public void leaveArena(Player p)
	{
		p.setScoreboard(manager.getNewScoreboard());
	}
	
	private void updateHealth()
	{
		updateTeamHealt(this.arena.getTeam().getTeam1());
		updateTeamHealt(this.arena.getTeam().getTeam2());
	}
	
	private void updateTeamHealt(WgTeam team)
	{
		for (TeamMember player : team.getTeamMembers())
		{
			if (player.getAlive())
			{
				board.getObjective("Lebensanzeige").getScore(player.getPlayer()).setScore(player.getPlayer().getHealth());
			}
			else
			{
				board.resetScores(player.getPlayer());
			}
		}
	}
	
	public void fightStateChanged()
	{
		if (this.arena.getFightState() == FightState.Running)
		{
			this.minutes = 0;
			initScoreboard();
			this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable(){
				public void run()
				{
					minuteUpdater();
				}
			}, 0, 1200);
		}
		else if (this.arena.getFightState() == FightState.Idle)
		{
			clearScoreboard();
			this.plugin.getServer().getScheduler().cancelTask(taskId);
		}
	}
}
