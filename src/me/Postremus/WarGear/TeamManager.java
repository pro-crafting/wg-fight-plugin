package me.Postremus.WarGear;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TeamManager implements Listener{

	List<TeamMember> players;
	WarGear plugin;
	Arena arena;
	
	public TeamManager(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		this.players = new ArrayList<TeamMember>();
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
		this.arena = arena;
	}
	
	public List<TeamMember> getTeamMembers()
	{
		return this.players;
	}
	
	public void setArena(Arena arena)
	{
		this.arena = arena;
	}
	
	public void quitFight()
	{
		for (TeamMember player : this.players)
		{
			player.getPlayer().getInventory().clear();
			player.getPlayer().teleport(this.plugin.getRepo().getEndWarpPoint(this.arena), TeleportCause.PLUGIN);
		}
		this.players = new ArrayList<TeamMember>();
	}
	
	public void GenerateWinnerTeamOutput(TeamNames teamName)
	{
		String team = "";
		if (teamName == TeamNames.Team1)
		{
			team = "[Team1]";
			for (TeamMember player : this.players)
			{
				if (player.getTeam() == TeamNames.Team1)
				{
					team += " "+ player.getPlayer().getName();
				}
			}
		}
		else if (teamName == TeamNames.Team2)
		{
			team = "[Team2]";
			for (TeamMember player : this.players)
			{
				if (player.getTeam() == TeamNames.Team2)
				{
					team += " "+ player.getPlayer().getName();
				}
			}
		}
		this.arena.broadcastMessage(ChatColor.DARK_GREEN + team + " hat gewonnen!");
	}
	
	public void GenerateTeamOutput()
	{
		String team1 = "[Team 1]";
		for (TeamMember player : this.players)
		{
			if (player.getTeam() == TeamNames.Team1)
			{
				team1 += " "+ player.getPlayer().getName();
			}
		}
		
		String team2 = "[Team 2]";
		for (TeamMember player : this.players)
		{
			if (player.getTeam() == TeamNames.Team2)
			{
				team2 += " "+ player.getPlayer().getName();
			}
		}
		
		this.arena.broadcastMessage(ChatColor.YELLOW +""+ ChatColor.ITALIC+team1 + " vs. " + team2);
	}
	
	public void setTeam(List<Player> teamMembers, TeamNames team)
	{
		List<TeamMember> toRemove = new ArrayList<TeamMember>();
		for(TeamMember player : this.players)
		{
			if (player.getTeam() == team)
			{
				toRemove.add(player);
			}
		}
		this.players.removeAll(toRemove);
		for(Player player : teamMembers)
		{
			this.players.add(new TeamMember(player, team));
		}
	}
	
	public Boolean isPlayerInTeam(String player, TeamNames team)
	{
		for(TeamMember member : this.players)
		{
			if (member.getPlayer().getName().equalsIgnoreCase(player) && member.getTeam() == team)
			{
				return true;
			}
		}
		return false;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
     public void deathEventHandler(PlayerDeathEvent event)
	 {
		 for (TeamMember player : this.players)
		 {
			 if (event.getEntity().getPlayer().getName().equalsIgnoreCase(player.getPlayer().getName()) && player.getAlive())
			 {
				 player.setAlive(false);
				 event.setDeathMessage(ChatColor.DARK_GREEN + player.getPlayer().getName() + "["+player.getTeam().toString()+"] ist gestorben.");
				 this.checkAlives(player.getTeam());
				 event.getDrops().clear();
			 }
		 }
	 }
	 
	 @EventHandler (priority = EventPriority.HIGHEST)
	 public void playerRespwanHandler(final PlayerRespawnEvent event)
	 {
		 if (arena == null)
		 {
			 return;
		 }
		 event.setRespawnLocation(this.plugin.getRepo().getEndWarpPoint(this.arena));
		 
		 this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				event.getPlayer().getInventory().clear();
			}
			 
		 }, 60);
	 }
	 
	 public Boolean checkAlives(TeamNames team)
	 {
		 Boolean someoneAlived = false;
		 for (TeamMember player : this.players)
		 {
			 if (player.getTeam() == team && player.getAlive())
			 {
				 someoneAlived = true;
			 }
		 }
		 if (!someoneAlived)
		 {
			 this.arena.broadcastMessage(ChatColor.DARK_GREEN + "Jeder aus dem ["+team.toString().toUpperCase()+"] ist tot.");
			 if (team == TeamNames.Team1)
			 {
				 this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "wgk quit team2");
			 }
			 else
			 {
				 this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "wgk quit team1");
			 }
		 }
		 return someoneAlived;
	 }
}
