package me.Postremus.WarGear.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.Postremus.WarGear.FightState;
import me.Postremus.WarGear.Arena.Arena;

public class FightStateChangedEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private FightState from;
	private FightState to;
	
	public FightStateChangedEvent(Arena arena, FightState from, FightState to)
	{
		this.arena = arena;
		this.from = from;
		this.to = to;
	}
	
    public Arena getArena()
    {
    	return this.arena;
    }
	
	public FightState getFrom()
	{
		return from;
	}
	
	public FightState getTo()
	{
		return to;
	}

	public void setTo(FightState to)
	{
		this.to = to;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
