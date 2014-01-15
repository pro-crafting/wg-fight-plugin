package me.Postremus.WarGear.Events;

import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.WgTeam;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FightQuitEvent  extends Event{
	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	
	public FightQuitEvent(Arena arena)
	{
		this.arena = arena;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public Arena getArena()
    {
    	return this.arena;
    }
}
