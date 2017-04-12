package de.pro_crafting.wg.event;

import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArenaStateChangeEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled;
  private Arena arena;
  private State from;
  private State to;

  public ArenaStateChangeEvent(Arena arena, State from, State to) {
    this.arena = arena;
    this.from = from;
    this.to = to;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public Arena getArena() {
    return this.arena;
  }

  public State getFrom() {
    return from;
  }

  public State getTo() {
    return to;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

}
