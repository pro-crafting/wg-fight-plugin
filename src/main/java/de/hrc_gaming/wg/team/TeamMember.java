package de.hrc_gaming.wg.team;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TeamMember{

	private UUID playerId;
	private Boolean alive;
	private Boolean isTeamLeader;
	
	public TeamMember(Player player, boolean isTeamLeader)
	{
		this.playerId = player.getUniqueId();
		this.alive = true;
		this.isTeamLeader = isTeamLeader;
	}
	
	public Player getPlayer()
	{
		return Bukkit.getPlayer(this.playerId);
	}
	
	public OfflinePlayer getOfflinePlayer()
	{
		return Bukkit.getOfflinePlayer(this.playerId);
	}
	
	public Boolean getAlive()
	{
		return this.alive;
	}
	
	public Boolean getIsTeamLeader()
	{
		return this.isTeamLeader;
	}
	
	public void setAlive(Boolean alive)
	{
		this.alive = alive;
	}
	
	public boolean isOnline()
	{
		return this.getOfflinePlayer().isOnline();
	}
}
