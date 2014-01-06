package me.Postremus.WarGear.Events;

import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.WgTeam;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FightQuitEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private WgTeam winnerTeam;
	private WgTeam looserTeam;
	private Arena arena;
	
	public FightQuitEvent(WgTeam winnerTeam, WgTeam looserTeam, Arena arena)
	{
		this.winnerTeam = winnerTeam;
		this.looserTeam = looserTeam;
		this.arena = arena;
	}
	
	@Override
	public HandlerList getHandlers() {
		return this.handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public WgTeam getWinnerTeam()
    {
    	return this.winnerTeam;
    }
    
    public WgTeam getLooserTeam()
    {
    	return this.looserTeam;
    }
    
    public Arena getArena()
    {
    	return this.arena;
    }
}
