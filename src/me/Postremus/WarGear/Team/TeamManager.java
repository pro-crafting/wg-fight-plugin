package me.Postremus.WarGear.Team;

import me.Postremus.WarGear.ArenaState;
import me.Postremus.WarGear.TeamWinReason;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.WarGearUtil;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Events.ArenaStateChangedEvent;
import me.Postremus.WarGear.Events.TeamWinQuitEvent;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;

public class TeamManager implements Listener
{
	private WarGear plugin;
	private Arena arena;
	private WgTeam team1;
	private WgTeam team2;
	
	public TeamManager(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		this.team1 = new WgTeam(TeamNames.Team1);
		this.team2 = new WgTeam(TeamNames.Team2);
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
		this.arena = arena;
	}
	
	public void setArena(Arena arena)
	{
		this.arena = arena;
	}
	
	public void prepareFightTeams()
	{
		this.prepareFightForTeam(team1);
		this.prepareFightForTeam(team2);
	}
	
	private void prepareFightForTeam(WgTeam team)
	{
		teleportTeamToTeamWarp(team.getTeamName());
		for (TeamMember player : team.getTeamMembers().values())
		{
			player.getPlayer().getInventory().clear();
			player.getPlayer().getInventory().setArmorContents(null);
			
		    player.getPlayer().setGameMode(GameMode.SURVIVAL);
			WarGearUtil.disableFly(player.getPlayer());
			WarGearUtil.makeHealthy(player.getPlayer());
			for (PotionEffect effect : player.getPlayer().getActivePotionEffects())
			{
				player.getPlayer().removePotionEffect(effect.getType());
			}
		}
	}
	
	public void setGameMode(TeamNames team, GameMode mode)
	{
		for (TeamMember player : this.getTeamOfName(team).getTeamMembers().values())
		{
			if (player.getPlayer() != null)
			{
				player.getPlayer().setGameMode(mode);
			}
		}
	}
	
	public void teleportTeamToTeamWarp(TeamNames team)
	{
		Location teleportTo = this.plugin.getRepo().getWarpForTeam(team, this.arena);
		for (TeamMember player : this.getTeamOfName(team).getTeamMembers().values())
		{
			if (player.getPlayer() != null)
			{
				player.getPlayer().teleport(teleportTo, TeleportCause.PLUGIN);
			}
		}
	}
	
	public void quitFight()
	{
		quiteFightForTeam(this.team1);
		quiteFightForTeam(this.team2);
		this.team1 = new WgTeam(TeamNames.Team1);
		this.team2 = new WgTeam(TeamNames.Team2);
	}
	
	private void quiteFightForTeam(WgTeam team)
	{
		for (TeamMember player : team.getTeamMembers().values())
		{
			player.getPlayer().getInventory().clear();
			player.getPlayer().teleport(this.arena.getRepo().getFightEndWarp(), TeleportCause.PLUGIN);
		}
	}
	
	public void GenerateWinnerTeamOutput(TeamNames teamName)
	{
		String team = "";
		if (teamName == TeamNames.Team1)
		{
			team = "[Team1]";
			for (String player : this.team1.getTeamMembers().keySet())
			{
				team += " "+ player;
			}
		}
		else if (teamName == TeamNames.Team2)
		{
			team = "[Team2]";
			for (String player : this.team2.getTeamMembers().keySet())
			{
				team += " "+ player;
			}
		}
		this.arena.broadcastMessage(ChatColor.DARK_GREEN + team + " hat gewonnen!");
	}
	
	public void GenerateTeamOutput()
	{
		String team1 = "[Team1]";
		for (String player : this.team1.getTeamMembers().keySet())
		{
			team1 += " "+ player;
		}
		
		String team2 = "[Team2]";
		for (String player : this.team2.getTeamMembers().keySet())
		{
			team2 += " "+ player;
		}
		
		this.arena.broadcastMessage(ChatColor.YELLOW +""+ ChatColor.ITALIC+team1 + " vs. " + team2);
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled=true)
     public void deathEventHandler(PlayerDeathEvent event)
	 {
		 if (arena.getFightState() != ArenaState.Running)
		 {
			 return;
		 }
		 if (!arena.contains(event.getEntity().getLocation()))
		 {
			 return;
		 }
		 Player died = event.getEntity();
		 final WgTeam team = this.getTeamOfPlayer(died);
		 if (team != null && team.getTeamMember(died).getAlive())
		 {
			 team.getTeamMember(died).setAlive(false);
			 event.setDeathMessage(ChatColor.DARK_GREEN + died.getName() + "["+team.getTeamName().toString()+"] ist gestorben.");
			 this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
			 {
				 public void run()
				 {
					 TeamManager.this.checkAlives(team);
				 }
			 });
		 }
	 }
	 
	 @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
	 public void playerRespawnHandler(PlayerRespawnEvent event)
	 {
		 final Player respawned = event.getPlayer();
		 if (!this.arena.contains(respawned.getLocation()))
		 {
			 return;
		 }
		 
		 event.setRespawnLocation(this.arena.getRepo().getFightEndWarp());
		 
		 this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable(){

			@Override
			public void run() {
				respawned.getInventory().clear();
			}
			 
		 }, 60);
	 }
	 
	 @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	 public void fightStateChangedHandler(ArenaStateChangedEvent event)
	 {
		 if (!event.getArena().equals(this.arena))
		{
			return;
		}
		 if (event.getTo() == ArenaState.Running)
		 {
			 this.healTeam(this.team1);
			 this.healTeam(this.team2);
		 }
	 }
	 
	 public void healTeam(WgTeam team)
	 {
		 for (TeamMember member : team.getTeamMembers().values())
		 {
			 WarGearUtil.makeHealthy(member.getPlayer());
		 }
	 }
	 
	 public WgTeam getTeamOfName(TeamNames name)
	 {
		 if (name == TeamNames.Team1)
		 {
			 return this.team1;
		 }
		 else
		 {
			 return this.team2;
		 }
	 }
	 
	 public WgTeam getTeamOfPlayer(Player p)
	 {
		 if (this.team1.getTeamMember(p) != null)
		 {
			 return this.team1;
		 }
		 else if (this.team2.getTeamMember(p) != null)
		 {
			 return this.team2;
		 }
		 return null;
	 }
	 
	 private void checkAlives(WgTeam team)
	 {
		 if (!team.isSomoneAlive())
		 {
			 WgTeam winnerTeam = this.team1;
			 if (team.getTeamName() == TeamNames.Team1)
			 {
				 winnerTeam = this.team2;
			 }
			 String message = "Jeder aus dem ["+team.getTeamName().toString().toUpperCase()+"] ist tot.";
			 this.plugin.getServer().getPluginManager().callEvent(new TeamWinQuitEvent(arena, message, winnerTeam, team, TeamWinReason.Death));
		 }
	 }
	 
	 public WgTeam getTeamWithOutLeader()
	 {
		 if (!this.team1.hasTeamLeader())
		 {
			 return this.team1;
		 }
		 else if (!this.team2.hasTeamLeader())
		 {
			 return this.team2;
		 }
		 return null;
	 }
	 
	 public void addTeamMember(TeamNames team, TeamMember member)
	 {
		 
	 }
	 
	 public boolean areBothTeamsReady()
	 {
		 return this.team1.getIsReady() && this.team2.getIsReady();
	 }
	 
	 public boolean isPlayerAlive(Player p)
	 {
		 if (this.getTeamOfPlayer(p) == null || this.getTeamOfPlayer(p).getTeamMember(p) == null)
		 {
			  return false;
		 }
		 return this.getTeamOfPlayer(p).getTeamMember(p).getAlive();
	 }
	 
	 public TeamMember getTeamMember(Player p)
	 {
		 if (this.team1.getTeamMember(p) != null)
		 {
			 return this.team1.getTeamMember(p);
		 }
		 if (this.team2.getTeamMember(p) != null)
		 {
			 return this.team2.getTeamMember(p);
		 }
		 return null;
	 }
	 
	 public WgTeam getTeam1()
	 {
		 return this.team1;
	 }
	 
	 public WgTeam getTeam2()
	 {
		 return this.team2;
	 }
}
