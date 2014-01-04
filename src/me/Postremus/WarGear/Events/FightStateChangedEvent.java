package me.Postremus.WarGear.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.Postremus.WarGear.FightState;

public class FightStateChangedEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private String arenaName;
	private FightState from;
	private FightState to;
	
	public FightStateChangedEvent(String arenaName, FightState from, FightState to)
	{
		this.from = from;
		this.to = to;
	}
	
	public String arenaName()
	{
		return arenaName;
	}
	
	public FightState getFrom()
	{
		return from;
	}
	
	public FightState getTo()
	{
		return to;
	}

	@Override
	public HandlerList getHandlers() {
		return this.handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
