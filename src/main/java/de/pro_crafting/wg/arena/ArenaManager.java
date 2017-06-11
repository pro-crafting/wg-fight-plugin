package de.pro_crafting.wg.arena;

import de.pro_crafting.wg.ErrorMessages;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.group.Group;
import de.pro_crafting.wg.group.PlayerGroupKey;
import de.pro_crafting.wg.group.PlayerRole;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class ArenaManager {

  private WarGear plugin;
  private Map<String, Arena> arenas;

  public ArenaManager(WarGear plugin) {
    this.plugin = plugin;
    this.arenas = new HashMap<>();
    this.loadArenas();
  }

  public void loadArenas() {
    this.arenas.clear();
    for (String element : this.plugin.getRepo().getArenaNames()) {
      loadArena(element);
    }
  }

  public void loadArena(String name) {
    this.plugin.getLogger().info("Lade Arena " + name);
    if (this.getArena(name) != null) {
      this.plugin.getLogger().info("Arena " + name + " ist bereits geladen.");
      return;
    }
    Arena arena = new Arena(this.plugin, name);
    ErrorMessages errors = arena.load();
    errors.getWarnings().forEach(Bukkit.getLogger()::warning);
    errors.getErrors().forEach(Bukkit.getLogger()::severe);
    if (!errors.hasErrors()) {
      this.plugin.getLogger().info("Arena " + name + " geladen.");
      this.arenas.put(name.toLowerCase(), arena);
      return;
    }
    this.plugin.getLogger().info("Arena " + name + " konnte nicht geladen werden.");
  }

  public void unloadArenas() {
    for (Arena arena : this.arenas.values()) {
      arena.unload();
    }
    this.arenas.clear();
  }

  public void unloadArena(String name) {
    Arena arena = this.getArena(name);
    if (arena != null) {
      arena.unload();
      this.arenas.remove(name.toLowerCase());
    }
  }

  public void saveArenas() {
    for (Arena arena : this.arenas.values()) {
      arena.getRepo().save();
    }
  }

  public Arena getArena(String name) {
    return arenas.get(name.toLowerCase());
  }

  public Collection<Arena> getArenas() {
    return this.arenas.values();
  }

  public Set<String> getArenaNames() {
    return this.arenas.keySet();
  }

  public boolean isArenaLoaded(String arenaName) {
    return getArena(arenaName) != null;
  }

  public Arena getArenaAt(Location where) {
    for (Arena arena : this.arenas.values()) {
      if (arena.contains(where)) {
        return arena;
      }
    }
    return null;
  }

  public Arena getArenaOfTeamMember(OfflinePlayer player) {
    for (Arena arena : this.arenas.values()) {
      if (arena.getGroupManager().getGroupMember(player) != null) {
        return arena;
      }
    }
    return null;
  }

  public PlayerGroupKey getGroup(OfflinePlayer player) {
    Arena arena = this.getArenaOfTeamMember(player);
    PlayerRole role = PlayerRole.Viewer;
    if (arena == null) {
      return new PlayerGroupKey(null, role);
    }
    Group team = arena.getGroupManager().getGroupOfPlayer(player);
    if (team != null) {
      role = arena.getGroupManager().getGroupOfPlayer(player).getRole();
    }
    return new PlayerGroupKey(arena, role);
  }
}
