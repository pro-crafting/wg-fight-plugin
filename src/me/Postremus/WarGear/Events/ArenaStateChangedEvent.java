package me.Postremus.WarGear.Events;

import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Arena.ArenaState;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArenaStateChangedEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private ArenaState from;
	private ArenaState to;
	
	public ArenaStateChangedEvent(Arena arena, ArenaState from, ArenaState to)
	{
		this.arena = arena;
		this.from = from;
		this.to = to;
	}
	
    public Arena getArena()
    {
    	return this.arena;
    }
	
	public ArenaState getFrom()
	{
		return from;
	}
	
	public ArenaState getTo()
	{
		return to;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
