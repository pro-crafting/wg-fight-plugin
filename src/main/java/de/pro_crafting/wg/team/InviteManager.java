package de.pro_crafting.wg.team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;

public class InviteManager {
	private Map<UUID, Invite> invites;
	private WarGear plugin;
	
	public InviteManager(WarGear plugin) {
		this.invites = new HashMap<UUID, Invite>();
		this.plugin = plugin;
	}
	
	public void addInvite(Player inviter, Player invited) {
		Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(inviter);
		if (invites.containsKey(invited.getUniqueId())) {
			inviter.sendMessage("§B"+invited.getDisplayName()+"§7 ist bereits eingeladen worden.");
			return;
		}
		
		this.invites.put(invited.getUniqueId(), new Invite(inviter.getUniqueId(), invited.getUniqueId(), 60));
		
		inviter.sendMessage("§7Du hast §B"+invited.getDisplayName()+"§7 eingeladen.");
		invited.sendMessage("§B"+inviter.getDisplayName()+"§7 hat dich zu einem Fight in §B"+arena.getName()+"§7 eingeladen.");
		invited.sendMessage("§7Mit §B\"/wgk team accept\"§7 nimmst du die Einladung an.");
		invited.sendMessage("§7Mit §B\"/wgk team deny\"§7 lehnst du die Einladung ab.");
	}
	
	private void timeInvites() {
		for (Invite invite : this.invites.values()) {
			invite.setRemainigTime(invite.getRemainigTime()-1);
			if (invite.getRemainigTime() == 0) {
				
			}
		}
	}
}
