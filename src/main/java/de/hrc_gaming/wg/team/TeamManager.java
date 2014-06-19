package de.hrc_gaming.wg.team;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.hrc_gaming.wg.FightQuitReason;
import de.hrc_gaming.wg.OfflineRunable;
import de.hrc_gaming.wg.Util;
import de.hrc_gaming.wg.WarGear;
import de.hrc_gaming.wg.arena.Arena;
import de.hrc_gaming.wg.arena.State;
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
		OfflineRunable fightTeamPreparer = new OfflineRunable() {
			
			public void run(TeamMember member) {
				Player player = member.getPlayer();
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				
				player.setGameMode(GameMode.SURVIVAL);
				Util.disableFly(player);
				Util.makeHealthy(player);
				Util.removePotionEffects(player);
				arena.teleport(player);
			}
		};
		this.plugin.getOfflineManager().run(fightTeamPreparer, team);
	}
	
	public Location getTeamSpawn(TeamNames team)
	{
		if (team == TeamNames.Team1)
		{
			return arena.getRepo().getTeam1Warp();
		}
		else
		{
			return arena.getRepo().getTeam2Warp();
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
		final Location teleportLocation = this.arena.getRepo().getFightEndWarp();
		OfflineRunable fightQuiter = new OfflineRunable() {
			
			public void run(TeamMember member) {
				member.getPlayer().getInventory().clear();
				member.getPlayer().teleport(teleportLocation, TeleportCause.PLUGIN);
			}
		};
		for (TeamMember member : team.getTeamMembers().values())
		{
			this.plugin.getOfflineManager().run(fightQuiter, member);
		}
	}
	
	public void sendWinnerOutput(TeamNames teamName)
	{
		String team = "["+teamName.toString()+"] "+concateTeamPlayers(this.getTeamOfName(teamName));
		this.arena.broadcastMessage(ChatColor.DARK_GREEN + team + " hat gewonnen!");
	}
	
	private String concateTeamPlayers(WgTeam team)
	{
		String ret = "";
		for (TeamMember member : team.getTeamMembers().values())
		{
			ret += member.getOfflinePlayer().getName()+ " ";
		}
		return ret.trim();
	}
	
	public void sendTeamOutput()
	{
		String team1 = "[Team1] "+concateTeamPlayers(this.getTeam1());
		String team2 = "[Team2] "+concateTeamPlayers(this.getTeam2());
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
	
	public void healTeam(WgTeam team)
	{
		OfflineRunable healer = new OfflineRunable() {
			public void run(TeamMember member) {
				Util.makeHealthy(member.getPlayer());
			}
		};
		for (TeamMember member : team.getTeamMembers().values())
		{
			this.plugin.getOfflineManager().run(healer, member);
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
	 
	public WgTeam getTeamOfPlayer(OfflinePlayer p)
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
	 
	public TeamMember getTeamMember(OfflinePlayer p)
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
