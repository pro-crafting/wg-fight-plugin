package de.hrc_gaming.wg.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.hrc_gaming.wg.arena.Arena;

public class FightQuitEvent  extends Event{
	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private String message;
	
	public FightQuitEvent(Arena arena, String message)
	{
		this.arena = arena;
		this.message = message;
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
    
    public String getMessage()
    {
    	return this.message;
    }
    
    public void setMessage(String message)
    {
    	this.message = message;
    }
}
