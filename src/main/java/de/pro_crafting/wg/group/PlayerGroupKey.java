package de.pro_crafting.wg.group;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.pro_crafting.wg.arena.Arena;

public class PlayerGroupKey {
	private Arena arena;
	private PlayerRole role;
	
	public PlayerGroupKey(Arena arena, PlayerRole role) {
		this.arena = arena;
		this.role = role;
	}
	
	public Arena getArena() {
		return this.arena;
	}
	
	public PlayerRole getRole() {
		return this.role;
	}
	
	public Group getGroup() {
		return getArena().getGroupManager().getTeamOfGroup(getRole());
	}
	
	public ProtectedRegion getRegion() {
		if (role == PlayerRole.Team1) {
			return this.arena.getRepo().getTeam1Region();
		} else if (role == PlayerRole.Team2) {
			return this.arena.getRepo().getTeam2Region();
		}
		return this.arena.getRepo().getArenaRegion();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.arena == null) ? 0 : this.arena.hashCode());
		result = prime * result
				+ ((this.role == null) ? 0 : this.role.hashCode());
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
		PlayerGroupKey other = (PlayerGroupKey) obj;
		if (this.arena == null) {
			if (other.arena != null)
				return false;
		} else if (!this.arena.equals(other.arena))
			return false;
		if (this.role != other.role)
			return false;
		return true;
	}
}
