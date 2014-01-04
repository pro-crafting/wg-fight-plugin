package me.Postremus.WarGear;

import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Events.FightQuitEvent;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
	WarGear plugin;
	Arena arena;
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
		for (TeamMember player : team.getTeamMembers())
		{
			player.getPlayer().getInventory().clear();
			player.getPlayer().getInventory().setArmorContents(null);
			
		    player.getPlayer().teleport(this.plugin.getRepo().getWarpForTeam(team.getTeamName(), this.arena), TeleportCause.PLUGIN);
		    player.getPlayer().setGameMode(GameMode.SURVIVAL);
			AdmincmdWrapper.disableFly(player.getPlayer());
			AdmincmdWrapper.heal(player.getPlayer());
			for (PotionEffect effect : player.getPlayer().getActivePotionEffects())
			{
				player.getPlayer().removePotionEffect(effect.getType());
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
		for (TeamMember player : team.getTeamMembers())
		{
			player.getPlayer().getInventory().clear();
			player.getPlayer().teleport(this.plugin.getRepo().getEndWarpPoint(this.arena), TeleportCause.PLUGIN);
		}
	}
	
	public void GenerateWinnerTeamOutput(TeamNames teamName)
	{
		String team = "";
		if (teamName == TeamNames.Team1)
		{
			team = "[Team1]";
			for (TeamMember player : this.team1.getTeamMembers())
			{
				team += " "+ player.getPlayer().getName();
			}
		}
		else if (teamName == TeamNames.Team2)
		{
			team = "[Team2]";
			for (TeamMember player : this.team2.getTeamMembers())
			{
				team += " "+ player.getPlayer().getName();
			}
		}
		this.arena.broadcastMessage(ChatColor.DARK_GREEN + team + " hat gewonnen!");
	}
	
	public void GenerateTeamOutput()
	{
		String team1 = "[Team1]";
		for (TeamMember player : this.team1.getTeamMembers())
		{
			team1 += " "+ player.getPlayer().getName();
		}
		
		String team2 = "[Team2]";
		for (TeamMember player : this.team2.getTeamMembers())
		{
			team2 += " "+ player.getPlayer().getName();
		}
		
		this.arena.broadcastMessage(ChatColor.YELLOW +""+ ChatColor.ITALIC+team1 + " vs. " + team2);
	}
	
	@EventHandler (priority = EventPriority.HIGH)
     public void deathEventHandler(PlayerDeathEvent event)
	 {
		Player died = event.getEntity().getPlayer();
		WgTeam team = this.getTeamOfPlayer(died);
		if (team != null && team.getTeamMember(died).getAlive())
		{
			team.getTeamMember(died).setAlive(false);
			event.setDeathMessage(ChatColor.DARK_GREEN + died.getName() + "["+team.getTeamName().toString()+"] ist gestorben.");
			this.checkAlives(team);
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
				event.getPlayer().getInventory().clear();
			}
			 
		 }, 60);
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
			 this.arena.broadcastMessage(ChatColor.DARK_GREEN + "Jeder aus dem ["+team.getTeamName().toString().toUpperCase()+"] ist tot.");
			 WgTeam looserTeam = this.team1;
			 if (team.getTeamName() == TeamNames.Team1)
			 {
				 looserTeam = this.team2;
			 }
			 this.plugin.getServer().getPluginManager().callEvent(new FightQuitEvent(team, looserTeam, this.arena));
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
	 
	 public WgTeam getTeam1()
	 {
		 return this.team1;
	 }
	 
	 public WgTeam getTeam2()
	 {
		 return this.team2;
	 }
}
