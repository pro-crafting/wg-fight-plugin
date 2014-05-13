package de.hrc_gaming.wg;

import net.gravitydevelopment.updater.Updater.UpdateResult;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class WgListener implements Listener {
	private WarGear plugin;
	
	public WgListener(WarGear plugin)
	{
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void handlePlayerJoinEvent(PlayerJoinEvent event)
	{
		if (!event.getPlayer().hasPermission("wargear.update"))
		{
			return;
		}
		if (this.plugin.getUpdater() != null && this.plugin.getUpdater().getResult() == UpdateResult.NO_UPDATE)
		{
			event.getPlayer().sendMessage("§7Version "+this.plugin.getUpdater().getLatestName()+" von "+this.plugin.getName()+" ist veröffentlicht.");
		}
	}
}
