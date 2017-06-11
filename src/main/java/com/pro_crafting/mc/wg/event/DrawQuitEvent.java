package com.pro_crafting.mc.wg.event;

import com.pro_crafting.mc.wg.group.Group;
import com.pro_crafting.mc.wg.FightQuitReason;
import com.pro_crafting.mc.wg.arena.Arena;

public class DrawQuitEvent extends FightQuitEvent {

  public DrawQuitEvent(Arena arena, String message, Group team1, Group team2,
                       FightQuitReason reason) {
    super(arena, message, team1, team2, reason);
  }

  public Group getTeam1() {
    return super.team1;
  }

  public Group getTeam2() {
    return super.team2;
  }
}
