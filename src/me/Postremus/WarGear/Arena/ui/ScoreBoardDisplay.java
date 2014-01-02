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
import me.Postremus.WarGear.TeamMember;
import me.Postremus.WarGear.TeamNames;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Events.FightStateChangedEvent;

public class ScoreBoardDisplay 
{
	private WarGear plugin;
	private Arena arena;
	private ScoreboardManager manager;
	private Scoreboard board;
	private Team teamRed;
	private Team teamBlue;
	private int seconds;
	
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
		for (TeamMember player : this.arena.getTeam().getTeamMembers())
		{
			if (player.getTeam() == TeamNames.Team1)
			{
				teamRed.addPlayer(player.getPlayer());
			}
			else
			{
				teamBlue.addPlayer(player.getPlayer());
			}
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
		seconds += 1;
		if ((seconds / 60) >= 1)
		{
			int minutes = (int)seconds/60;
			board.getObjective("Lebensanzeige").getScore(this.plugin.getServer().getOfflinePlayer(ChatColor.GREEN+"Dauer (m):")).setScore(minutes);
		}
		else
		{
			board.getObjective("Lebensanzeige").getScore(this.plugin.getServer().getOfflinePlayer(ChatColor.GREEN+"Dauer (m):")).setScore(0);
		}
		for (Player p : this.plugin.getServer().getOnlinePlayers())
		{
			p.setScoreboard(manager.getNewScoreboard());
		}
		for (Player p : this.arena.getPlayersInArena())
		{
			p.setScoreboard(board);
		}
		updateHealth();
	}
	
	private void updateHealth()
	{
		for (TeamMember player : this.arena.getTeam().getTeamMembers())
		{
			board.getObjective("Lebensanzeige").getScore(player.getPlayer()).setScore(player.getPlayer().getHealth());
		}
	}
	
	public void fightStateChanged()
	{
		if (this.arena.getFightState() == FightState.Running)
		{
			this.seconds = 0;
			initScoreboard();
		}
		else if (this.arena.getFightState() == FightState.Idle)
		{
			clearScoreboard();
		}
	}
}
