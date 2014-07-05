package de.pro_crafting.invitations;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import de.pro_crafting.wg.WarGear;

public class InvitationSystem {
	private WarGear plugin;
	private List<Invitation> invitations;
	private int taskId;
	
	public InvitationSystem(WarGear plugin)
	{
		this.plugin = plugin;
		this.invitations = new ArrayList<Invitation>();
		taskId = -1;
	}
	
	public void add(Invitation inv)
	{
		this.invitations.add(inv);
		if (taskId == -1)
		{
			this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable()
			{
				public void run()
				{
					updateInvitations();
				}
			}, 0, 100);
		}
	}
	
	private void remove(Invitation inv)
	{
		this.invitations.remove(inv);
		if (this.invitations.size() == 0)
		{
			this.plugin.getServer().getScheduler().cancelTask(this.taskId);
			this.taskId = -1;
		}
	}
	
	public void accept(String toPlayer)
	{
		Invitation curr = getInvitationToPlayer(toPlayer);
		if (curr != null)
		{
			curr.updateState(InvitationState.Rejected);
		}
	}
	
	public void reject(String toPlayer)
	{
		Invitation curr = getInvitationToPlayer(toPlayer);
		if (curr != null)
		{
			curr.updateState(InvitationState.Rejected);
		}
	}
	
	public Invitation getInvitationToPlayer(String player)
	{
		for (Invitation curr : this.invitations)
		{
			if (curr.getToPlayer().equalsIgnoreCase(player))
			{
				return curr;
			}
		}
		return null;
	}
	
	private void updateInvitations()
	{
		for (int i=invitations.size(); i>=0;i--)
		{
			if (invitations.get(i).getDuration() <= 0)
			{
				Invitation inv = invitations.get(i);
				Player p = this.plugin.getServer().getPlayer(inv.getToPlayer());
				if (p != null)
				{
					p.sendMessage("Einladung von "+inv.getFromPlayer()+" ist abgelaufen.");
				}
				invitations.get(i).updateState(InvitationState.Expired);
				this.remove(invitations.get(i));
			}
			else
			{
				invitations.get(i).setDuration(invitations.get(i).getDuration()+5);
			}
		}
	}
}
