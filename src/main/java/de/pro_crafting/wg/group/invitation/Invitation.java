package de.pro_crafting.wg.group.invitation;

import de.pro_crafting.wg.group.GroupMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class Invitation {
	protected GroupMember inviter;
	protected UUID invited;
	protected int remainigTime;
	protected InvitationType type;

	public Invitation(GroupMember inviter, UUID invited, int remainigTime, InvitationType type) {
		this.inviter = inviter;
		this.invited = invited;
		this.remainigTime = remainigTime;
		this.type = type;
	}

	public GroupMember getInviter() {
		return inviter;
	}

	public UUID getInvited() {
		return invited;
	}

	public int getRemainigTime() {
		return remainigTime;
	}

	public void setRemainigTime(int remainigTime) {
		this.remainigTime = remainigTime;
	}

	public InvitationType getType() {
		return type;
	}

	public void sendInvitationDescription() {
		Player player = Bukkit.getPlayer(invited);
		if (player != null) {
			player.sendMessage("§7Du hast 60 Sekunden zum Annehmen.");
			player.sendMessage("§7Mit §B\"/wgk team accept\"§7 nimmst du die Einladung an.");
			player.sendMessage("§7Mit §B\"/wgk team decline\"§7 lehnst du die Einladung ab.");
		}
	}

	public void sendAcceptMessages(Player invited) {
	}

	public void sendDeclineMessages(Player invited) {
		if (invited != null) {
			if (inviter.isOnline()) {
				inviter.getPlayer().sendMessage("§B" + invited.getDisplayName() + "§7 hat deine Einladung abgelehnt.");
			}

			invited.sendMessage("§7Du hast die Einladung von §B" + inviter.getName() + "§7 abgelehnt.");
		}
	}
}

