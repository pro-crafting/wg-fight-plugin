package de.pro_crafting.wg;

import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.ArenaPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WgRegionListener implements Listener {

  private WarGear plugin;

  public WgRegionListener(WarGear plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void entityExplodeHandler(EntityExplodeEvent event) {
    Location location = event.getLocation();
    Arena arenaAt = plugin.getArenaManager().getArenaAt(location);
    if (arenaAt == null) {
      return;
    }

    for (Block block : event.blockList()) {
      ArenaPosition position = arenaAt.getPosition(block.getLocation());
      if (position == ArenaPosition.Outside) {
        continue;
      }

      if (position == ArenaPosition.Platform) {
        event.setCancelled(true);
        return;
      }

      if (!arenaAt.isOpen()) {
        event.setCancelled(true);
        break;
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void entityDamageByEntityHandler(EntityDamageByEntityEvent event) {
    Location location = event.getEntity().getLocation();
    Arena arenaAt = plugin.getArenaManager().getArenaAt(location);
    if (arenaAt == null) {
      return;
    }

    ArenaPosition position = arenaAt.getPosition(location);
    if (position == ArenaPosition.Platform) {
      event.setCancelled(true);
    }

    if (position != ArenaPosition.Outside) {
      if (!arenaAt.isOpen()) {
        event.setCancelled(true);
      }
    }
  }

  private boolean isAllowed(Location loc) {
    Location location = loc;
    Arena arenaAt = plugin.getArenaManager().getArenaAt(location);
    if (arenaAt == null) {
      return true;
    }

    ArenaPosition position = arenaAt.getPosition(location);
    if (position == ArenaPosition.Platform) {
      return false;
    }

    if (position == ArenaPosition.Team1PlayField || position == ArenaPosition.Team2PlayField) {
      return false;
    }

    if (position == ArenaPosition.Team1WG || position == ArenaPosition.Team2WG) {
      if (!arenaAt.isOpen()) {
        return false;
      }
    }

    return true;
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void blockIgniteHandler(BlockIgniteEvent event) {
    if (!isAllowed(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void blockBurnHandler(BlockBurnEvent event) {
    if (!isAllowed(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void blockSpreadHandler(BlockSpreadEvent event) {
    if (event.getSource().getType() == Material.FIRE) {
      if (!isAllowed(event.getSource().getLocation())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void blockBreakHandler(BlockBreakEvent event) {
    if (!isAllowed(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void blockPlaceHandler(BlockPlaceEvent event) {
    if (!isAllowed(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }
}
