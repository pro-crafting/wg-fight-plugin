package de.pro_crafting.wg.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pro_crafting.wg.arena.Arena;

public class PlayerArenaChangeEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private Arena from;
	private Arena to;
	private String message;
	
	public PlayerArenaChangeEvent(Player player, Arena from, Arena to) {
		this.player = player;
		this.from = from;
		this.to = to;
		this.message = "";
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public Arena getFrom() {
		return this.from;
	}
	
	public Arena getTo() {
		return this.to;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
