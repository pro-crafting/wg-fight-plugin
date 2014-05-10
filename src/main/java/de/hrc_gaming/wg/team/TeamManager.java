package de.hrc_gaming.wg.team;

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

import de.hrc_gaming.wg.FightQuitReason;
import de.hrc_gaming.wg.Util;
import de.hrc_gaming.wg.WarGear;
import de.hrc_gaming.wg.arena.Arena;
import de.hrc_gaming.wg.arena.State;
import de.hrc_gaming.wg.event.ArenaStateChangedEvent;
import de.hrc_gaming.wg.event.WinQuitEvent;

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
	
	public void prepareFightTeams()
	{
		this.prepareFightForTeam(team1);
		this.prepareFightForTeam(team2);
	}
	
	private void prepareFightForTeam(WgTeam team)
	{
		Location teamWarp = this.plugin.getRepo().getWarpForTeam(team.getTeamName(), this.arena);
		for (TeamMember player : team.getTeamMembers().values())
		{
			player.getPlayer().getInventory().clear();
			player.getPlayer().getInventory().setArmorContents(null);
			
		    player.getPlayer().setGameMode(GameMode.SURVIVAL);
			Util.disableFly(player.getPlayer());
			Util.makeHealthy(player.getPlayer());
			Util.removePotionEffects(player.getPlayer());
			player.getPlayer().teleport(teamWarp, TeleportCause.PLUGIN);
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
			if (player.getPlayer() != null)
			{
				player.getPlayer().getInventory().clear();
				player.getPlayer().teleport(this.arena.getRepo().getFightEndWarp(), TeleportCause.PLUGIN);
			}
		}
	}
	
	public void sendWinnerOutput(TeamNames teamName)
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
	
	public void sendTeamOutput()
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
		 if (arena.getState() != State.Running)
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

			public void run() {
				respawned.getInventory().clear();
			}
			 
		 }, 60);
	 }
	 
	 @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	 public void arenaStateChangedHandler(ArenaStateChangedEvent event)
	 {
		 if (!event.getArena().equals(this.arena))
		{
			return;
		}
		 if (event.getTo() == State.Running)
		 {
			 this.healTeam(this.team1);
			 this.healTeam(this.team2);
		 }
	 }
	 
	 public void healTeam(WgTeam team)
	 {
		 for (TeamMember member : team.getTeamMembers().values())
		 {
			 Util.makeHealthy(member.getPlayer());
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
			 this.plugin.getServer().getPluginManager().callEvent(new WinQuitEvent(arena, message, winnerTeam, team, FightQuitReason.Death));
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

	public WgTeam getTeam1() {
		return team1;
	}

	public WgTeam getTeam2() {
		return team2;
	}
}
