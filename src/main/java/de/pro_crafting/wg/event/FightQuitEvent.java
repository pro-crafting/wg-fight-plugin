package de.pro_crafting.wg.event;

import de.pro_crafting.wg.FightQuitReason;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.group.Group;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class FightQuitEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  protected Group team1;
  protected Group team2;
  private Arena arena;
  private String message;
  private FightQuitReason reason;

  public FightQuitEvent(Arena arena, String message, Group team1, Group team2,
      FightQuitReason reason) {
    this.arena = arena;
    this.message = message;
    this.team1 = team1;
    this.team2 = team2;
    this.reason = reason;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public Arena getArena() {
    return this.arena;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public FightQuitReason getReason() {
    return this.reason;
  }
}
