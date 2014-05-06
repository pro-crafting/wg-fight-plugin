package de.hrc_gaming.wg.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.hrc_gaming.wg.FightQuitReason;
import de.hrc_gaming.wg.arena.Arena;
import de.hrc_gaming.wg.team.WgTeam;

public abstract class FightQuitEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private String message;
	protected WgTeam team1;
	protected WgTeam team2;
	private FightQuitReason reason;
	
	public FightQuitEvent(Arena arena, String message, WgTeam team1, WgTeam team2, FightQuitReason reason)
	{
		this.arena = arena;
		this.message = message;
		this.team1 = team1;
		this.team2 = team2;
		this.reason = reason;
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
    
    public FightQuitReason getReason()
    {
    	return this.reason;
    }
}
