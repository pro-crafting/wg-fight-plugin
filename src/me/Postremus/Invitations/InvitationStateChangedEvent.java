package me.Postremus.Invitations;

import java.io.File;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.protection.managers.RegionManager;

public class InvitationStateChangedEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private Invitation invitation;
	private InvitationState from;
	private InvitationState to;
	
	public InvitationStateChangedEvent(Invitation invitation, InvitationState from, InvitationState to)
	{
		this.invitation = invitation;
		this.from = from;
		this.to = to;
	}
	
	public Invitation getInivite()
	{
		return this.invitation;
	}
	
	public InvitationState getFrom()
	{
		return this.from;
	}
	
	public InvitationState getTo()
	{
		return this.to;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
