package de.hrc_gaming.wg;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import de.hrc_gaming.wg.arena.Arena;

public class OfflineListener implements Listener{
	private BukkitTask task;
	private WarGear plugin;
	private List<SimpleEntry<UUID, Integer>> quitTimes;
	
	public OfflineListener(WarGear plugin) {
		this.plugin = plugin;
		this.quitTimes = new ArrayList<SimpleEntry<UUID,Integer>>();
		Bukkit.getPluginManager().registerEvents(this, this.plugin);
	}

	@EventHandler
	public void playerQuitHandler(PlayerQuitEvent event)
	{
		if (this.plugin.getArenaManager().getArenaOfTeamMember(event.getPlayer()) == null)
		{
			return;
		}
		quitTimes.add(new SimpleEntry<UUID, Integer>(event.getPlayer().getUniqueId(), 0));
		if (task == null)
		{
			task = Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable(){
				public void run()
				{
					offlineKickCheck();
				}
			}, 1000, 1000);
		}
	}
	
	@EventHandler
	public void playerJoinHandler(PlayerJoinEvent event)
	{
		quitTimes.remove(event.getPlayer().getUniqueId());
		if (task != null && quitTimes.size() == 0)
		{
			task.cancel();
		}
	}
	
	private void offlineKickCheck()
	{
		for (int i=quitTimes.size()-1;i>=0;i--)
		{
			if (quitTimes.get(i).getValue() == 30)
			{
				OfflinePlayer player = Bukkit.getOfflinePlayer(quitTimes.get(i).getKey());
				Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(player);
				arena.broadcastMessage(player+" ist offline. Er wird daher gekickt.");
				arena.getTeam().getTeamOfPlayer(player).remove(player);
				quitTimes.remove(i);
			}
			else
			{
				quitTimes.get(i).setValue(quitTimes.get(i).getValue()+1);
			}
		}
		if (quitTimes.size() == 0)
		{
			task.cancel();
		}
	}
}
