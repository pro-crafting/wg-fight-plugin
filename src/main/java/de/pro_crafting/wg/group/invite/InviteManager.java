package de.pro_crafting.wg.group.invite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.group.GroupMember;
import de.pro_crafting.wg.group.PlayerGroupKey;

public class InviteManager {
	private Map<UUID, Invite> invites;
	private WarGear plugin;
	
	public InviteManager(WarGear plugin) {
		this.invites = new HashMap<UUID, Invite>();
		this.plugin = plugin;
		
		Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable() {
			public void run() {
				doInviteTimeCheck();
			}
		}, 0, 20);
	}
	
	public void addInvite(PlayerGroupKey key, Player invited) {
		Arena arena = key.getArena();
		
		GroupMember teamLeader = key.getGroup().getLeader();
		
		if (invites.containsKey(invited.getUniqueId())) {
			if (teamLeader.isOnline()) {
				teamLeader.getPlayer().sendMessage("§B"+invited.getDisplayName()+"§7 wurde bereits eingeladen.");
			}
			return;
		}
		
		this.invites.put(invited.getUniqueId(), new Invite(key, invited.getUniqueId(), 60));
		
		if (teamLeader.isOnline()) {
			teamLeader.getPlayer().sendMessage("§7Du hast §B"+invited.getDisplayName()+"§7 eingeladen.");
		}
		invited.sendMessage("§B"+teamLeader.getName()+"§7 hat dich zu einem Fight in §B"+arena.getName()+"§7 eingeladen.");
		invited.sendMessage("§7Du hast 60 Sekunden zum Annehmen.");
		invited.sendMessage("§7Mit §B\"/wgk team accept\"§7 nimmst du die Einladung an.");
		invited.sendMessage("§7Mit §B\"/wgk team decline\"§7 lehnst du die Einladung ab.");
	}
	
	public void acceptInvite(Player invited) {
		Invite inv = invites.get(invited.getUniqueId());
		if (inv == null) {
			invited.sendMessage("§7Du hast zurzeit keine offene Einladung.");
			return;
		}
		
		Arena arena = inv.getGroupKey().getArena();
		if (arena.getState() != State.Setup) {
			invited.sendMessage("§7Dieser Fight läuft bereits.");
			invites.remove(invited.getUniqueId());
			return;
		}
		
		GroupMember teamLeader = inv.getGroupKey().getGroup().getLeader();
		
		if (teamLeader.isOnline()) {
			teamLeader.getPlayer().sendMessage("§B"+invited.getDisplayName()+"§7 ist deinem Team beigetreten.");
		}
		invited.sendMessage("§7Du hast die Einladung von §B"+teamLeader.getName()+"§7 angenommen.");
		invited.sendMessage("§7Mit §8\"/wgk team leave\" §7verlässt du das Team.");
		invites.remove(invited.getUniqueId());
		
		inv.getGroupKey().getGroup().add(invited, false);
		this.plugin.getScoreboard().addTeamMember(arena, inv.getGroupKey().getGroup().getMember(invited), inv.getGroupKey().getRole());
	}
	
	public void declineInvite(Player invited) {
		Invite inv = invites.get(invited.getUniqueId());
		if (inv == null) {
			invited.sendMessage("§7Du hast zurzeit keine offene Einladung.");
			return;
		}
		
		GroupMember teamLeader = inv.getGroupKey().getGroup().getLeader();
		
		if (teamLeader.isOnline()) {
			teamLeader.getPlayer().sendMessage("§B"+invited.getDisplayName()+"§7 hat deine Einladung abgelehnt.");
		}
		
		invited.sendMessage("§7Du hast die Einladung von §B"+teamLeader.getName()+"§7 abgelehnt.");
		invites.remove(invited.getUniqueId());
	}
	
	private void doInviteTimeCheck() {
		Iterator<Entry<UUID, Invite>> inviteIt = this.invites.entrySet().iterator();
		while (inviteIt.hasNext()) {
			Entry<UUID, Invite> next = inviteIt.next();
			Invite invite = next.getValue();
			
			if (invite.getRemainigTime() == 0) {
				GroupMember teamleader = invite.getGroupKey().getGroup().getLeader();
				OfflinePlayer invited = Bukkit.getOfflinePlayer(invite.getInvited());
				if (teamleader.isOnline()) {
					teamleader.getPlayer().sendMessage("§B"+invited.getName()+"§7 hat deine Einladung nicht angenommen.");
				}
				if (invited.isOnline()) {
					((Player)invited).sendMessage("§7Die Einladung von §B" +teamleader.getName()+ "§7 ist abgelaufen.");
				}
				inviteIt.remove();
				continue;
			}
			invite.setRemainigTime(invite.getRemainigTime()-1);
		}
	}
	
	
}
