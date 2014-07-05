package de.pro_crafting.wg.team;

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
	
	public Boolean isAlive()
	{
		return this.alive;
	}
	
	public Boolean isTeamLeader()
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.alive == null) ? 0 : this.alive.hashCode());
		result = prime
				* result
				+ ((this.isTeamLeader == null) ? 0 : this.isTeamLeader
						.hashCode());
		result = prime * result
				+ ((this.playerId == null) ? 0 : this.playerId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TeamMember other = (TeamMember) obj;
		if (this.alive == null) {
			if (other.alive != null)
				return false;
		} else if (!this.alive.equals(other.alive))
			return false;
		if (this.isTeamLeader == null) {
			if (other.isTeamLeader != null)
				return false;
		} else if (!this.isTeamLeader.equals(other.isTeamLeader))
			return false;
		if (this.playerId == null) {
			if (other.playerId != null)
				return false;
		} else if (!this.playerId.equals(other.playerId))
			return false;
		return true;
	}
}
