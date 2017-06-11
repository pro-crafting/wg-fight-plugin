package de.pro_crafting.wg.modes;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import java.util.HashMap;
import java.util.Map;

public class ModeManager {

  private Map<String, Class<? extends FightMode>> modes;
  private WarGear plugin;

  public ModeManager(WarGear plugin) {
    this.modes = new HashMap<>();
    this.plugin = plugin;

    this.modes.put("kit", KitMode.class);
    this.modes.put("chest", ChestMode.class);
  }

  public void add(String mode, Class<? extends FightMode> clazz) {
    this.modes.put(mode.toLowerCase(), clazz);
  }

  public void remove(String mode) {
    this.modes.remove(mode.toLowerCase());
  }

  public FightMode get(String mode, Arena arena) {
    Class<? extends FightMode> clazz = modes.get(mode.toLowerCase());
    if (clazz == null) {
      return null;
    }

    try {
      return clazz.getConstructor(WarGear.class, Arena.class).newInstance(plugin, arena);
    } catch (Exception ex) {
      return null;
    }
  }
}
