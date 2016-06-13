package de.pro_crafting.wg.group.invitation;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.group.Group;
import de.pro_crafting.wg.group.GroupMember;
import de.pro_crafting.wg.group.PlayerGroupKey;
import de.pro_crafting.wg.group.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class InvitationManager {
	private Map<UUID, Invitation> invites;
	private JavaPlugin plugin;

	public InvitationManager(JavaPlugin plugin) {
		this.invites = new HashMap<UUID, Invitation>();
		this.plugin = plugin;

		Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable() {
			public void run() {
				doInvitationTimeCheck();
			}
		}, 0, 20);
	}

	public void addInvitation(GroupMember leader, Player invited, InvitationType type) {
		if(!leader.isLeader()){
			if (leader.isOnline()) {
				leader.getPlayer().sendMessage("§7Du darfst als Member keine Einladungen erstellen!.");
			}
			return;
		}

		Group group = getGroup(invited);
		if(group != null && group.getMember(invited).isLeader()){
			if (leader.isOnline()) {
				leader.getPlayer().sendMessage("§b" + invited.getDisplayName() + " §7 ist bereits im Team von §b" + group.getLeader().getName() + "§b!");
			}
			return;
		}
		if ( hasInvitation( invited.getUniqueId() )){
			if (leader.isOnline()) {
				leader.getPlayer().sendMessage("§B"+invited.getDisplayName()+"§7 wurde bereits eingeladen.");
			}
			return;
		}

		if ( hasInvitation( leader.getPlayer().getUniqueId() )){
			if (leader.isOnline()) {
				leader.getPlayer().sendMessage("§7Du hast bereits einen Spieler eingeladen.");

			}
			return;
		}
		Invitation inv = type.create(leader, invited.getUniqueId(), 60);
		if (inv != null) {
			this.invites.put(invited.getUniqueId(), inv);

			inv.sendInvitationDescription();
		}
	}

	public Group getGroup(Player invited) {
		PlayerGroupKey group = WarGear.getPlugin(WarGear.class).getArenaManager().getGroup(invited);
		return group.getRole() == PlayerRole.Viewer ? null : group.getGroup();
	}

	public boolean hasInvitation(OfflinePlayer leader, Player invited, InvitationType type) {
		Invitation inv = invites.get(invited.getUniqueId());
		return inv != null
				&& inv.getInviter().getOfflinePlayer().equals(leader)
				&& inv.getType() == type;
	}

	protected boolean hasInvitation(UUID player){
		return getInvitation(Bukkit.getOfflinePlayer(player)) != null ||
				getInvitationOfLeader(Bukkit.getOfflinePlayer(player)) != null;
	}

	protected Invitation getInvitation(OfflinePlayer player) {
		return invites.get(player.getUniqueId());
	}

	protected Invitation getInvitationOfLeader(OfflinePlayer leader) {
		for (Invitation invitation : this.invites.values()) {
			if (invitation.getInviter().getOfflinePlayer().equals(leader)) {
				return invitation;
			}
		}
		return null;
	}

	public void acceptInvite(Player invited) {
		Invitation inv = invites.get(invited.getUniqueId());
		removeInvitation(inv);
		if (inv == null) {
			invited.sendMessage("§7Du hast zurzeit keine offene Einladung.");
			return;
		}

		inv.sendAcceptMessages(invited);
	}

	public void declineInvitation(Player invited) {
		Invitation inv = this.getInvitation(invited);
		this.removeInvitation(null, invited.getUniqueId());

		if (inv == null) {
			invited.sendMessage("§7Du hast zurzeit keine offene Einladung.");
			return;
		}

		inv.sendDeclineMessages(invited);
		this.removeInvitation(inv);
	}

	public void cancelInvitation(Player leader) {
		Invitation inv = this.getInvitationOfLeader(leader);
		this.removeInvitation(leader.getUniqueId(), null);

		if (inv == null) {
			leader.sendMessage("§7Du hast zurzeit keine offene Einladung.");
			return;
		}

		Player invited = Bukkit.getPlayer( inv.getInvited());
		if (invited != null) {
			invited.sendMessage("§B" + leader.getDisplayName() + "§7 hat die Anfrage abgebrochen!");
			leader.sendMessage("§7Du hast die Anfrage an §B"+invited.getDisplayName()+"§7 abgebrochen!");
		}
		this.removeInvitation(inv);
	}

	protected void removeInvitation(Invitation inv){
		if (inv == null) {
			return;
		}
		this.removeInvitation(inv.getInviter().getOfflinePlayer().getUniqueId(), inv.getInvited());
	}

	protected void removeInvitation(UUID inviter, UUID invited) {
		this.invites.remove(invited);
	}

	protected void doInvitationTimeCheck() {
		Iterator<Map.Entry<UUID, Invitation>> inviteIt = this.invites.entrySet().iterator();
		while (inviteIt.hasNext()) {
			Invitation invitation = inviteIt.next().getValue();

			if (invitation.getRemainigTime() == 0) {
				invitationTimedOut(invitation);
				inviteIt.remove();
			} else {
				invitation.setRemainigTime(invitation.getRemainigTime() - 1);
			}
		}
	}

	protected void invitationTimedOut(Invitation invitation) {
		GroupMember teamleader = invitation.getInviter();
		OfflinePlayer invited = Bukkit.getOfflinePlayer(invitation.getInvited());
		if (teamleader.isOnline()) {
			teamleader.getPlayer().sendMessage("§B"+invited.getName()+"§7 hat deine Einladung nicht angenommen.");
		}
		if (invited.isOnline()) {
			((Player)invited).sendMessage("§7Die Einladung von §B" +teamleader.getName()+ "§7 ist abgelaufen.");
		}
	}
}