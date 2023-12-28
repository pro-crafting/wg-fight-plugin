package com.pro_crafting.mc.wg.group;

import com.pro_crafting.mc.wg.arena.Arena;
import com.pro_crafting.mc.wg.model.WgRegion;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
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

  public WgRegion getRegion() {
    if (role == PlayerRole.Team1) {
      return this.arena.getRepo().getTeam1Region();
    } else if (role == PlayerRole.Team2) {
      return this.arena.getRepo().getTeam2Region();
    }
    return this.arena.getRepo().getArenaRegion();
  }
}
