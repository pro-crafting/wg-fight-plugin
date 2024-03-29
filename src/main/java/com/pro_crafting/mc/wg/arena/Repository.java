package com.pro_crafting.mc.wg.arena;

import com.google.common.collect.Sets;
import com.pro_crafting.mc.common.Point;
import com.pro_crafting.mc.wg.group.GroupSide;
import com.pro_crafting.mc.wg.ErrorMessages;
import com.pro_crafting.mc.wg.Util;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.model.WgRegion;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class Repository {

  private WarGear plugin;
  private File arenaConfig;
  private YamlConfiguration config;

  @Delegate
  private ArenaConfiguration configuration;

  public Repository(WarGear plugin, Arena arena) {
    this.plugin = plugin;
    this.arenaConfig = new File(this.plugin.getArenaFolder(), arena.getName() + ".yml");
  }

  public ErrorMessages load() {
    if (!this.arenaConfig.exists()) {
      Sets.newHashSet("Configuration does not exist");
    }
    this.config = YamlConfiguration.loadConfiguration(this.arenaConfig);
    this.configuration = new ArenaConfiguration(this.config.getValues(true));
    return configuration.getErrors();
  }

  public boolean save() {
    return false;
  }

  public WgRegion getTeamRegion(GroupSide side) {
    return side == GroupSide.Team1 ? getTeam1Region() : getTeam2Region();
  }

  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  public class ArenaConfiguration {

    public static final String WORLD = "world";
    public static final String MODE = "mode";
    public static final String AUTO_RESET = "auto-reset";
    public static final String WATER_REMOVE = "water-remove";
    public static final String FOOD_LEVEL_CHANGE = "food-level-change";
    public static final String SCOREBOARD_ENABLED = "scoreboard.enabled";
    public static final String SCOREBOARD_TIME = "scoreboard.time";
    public static final String SPECTATOR_MODE_ENABLED = "spectator-mode.enabled";
    public static final String SPECTATOR_MODE_TIME = "spectator-mode.time";
    public static final String PREFIX_TEAM1 = "prefix.team1";
    public static final String PREFIX_TEAM2 = "prefix.team2";
    public static final String GROUND_HEIGHT = "ground.height";
    public static final String GROUND_DAMAGE = "ground.damage";
    public static final String GROUND_SCHEMATIC = "ground.schematic";
    public static final String REGIONS_ARENA_MIN = "regions.arena.min";
    public static final String REGIONS_ARENA_MAX = "regions.arena.max";
    public static final String REGIONS_INNER_MIN = "regions.inner.min";
    public static final String REGIONS_INNER_MAX = "regions.inner.max";
    public static final String REGIONS_TEAM1_MIN = "regions.team1.min";
    public static final String REGIONS_TEAM1_MAX = "regions.team1.max";
    public static final String REGIONS_TEAM2_MIN = "regions.team2.min";
    public static final String REGIONS_TEAM2_MAX = "regions.team2.max";
    public static final String FIGHT_START_TEAM1 = "fightStart.team1";
    public static final String FIGHT_START_TEAM2 = "fightStart.team2";
    public static final String SPAWN = "spawn";

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.PACKAGE)
    private ErrorMessages errors;

    private WgRegion arenaRegion;
    private WgRegion innerRegion;
    private WgRegion team1Region;
    private WgRegion team2Region;

    private Location team1Warp;
    private Location team2Warp;
    private Location spawnWarp;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String worldName;
    private String fightMode;
    private int groundHeight;
    private String groundSchematic;
    private boolean isAutoReset;
    private boolean waterRemove;
    private boolean foodLevelChange;
    private int groundDamage;
    private boolean isScoreboardEnabled;
    private int scoreboardTime;
    private boolean isSpectatorModeEnabled;
    private int spectatorModeTime;
    private String team1Prefix;
    private String team2Prefix;

    public ArenaConfiguration() {
      errors = new ErrorMessages();
    }

    public ArenaConfiguration(Map<String, Object> values) {
      errors = new ErrorMessages();
      worldName = getWorldName(values, WORLD, errors);
      fightMode = get(values, MODE, "kit", errors);
      isAutoReset = get(values, AUTO_RESET, true, errors);
      waterRemove = get(values, WATER_REMOVE, true, errors);
      foodLevelChange = get(values, FOOD_LEVEL_CHANGE, true, errors);
      isScoreboardEnabled = get(values, SCOREBOARD_ENABLED, true, errors);
      scoreboardTime = get(values, SCOREBOARD_TIME, 30, errors);
      isSpectatorModeEnabled = get(values, SPECTATOR_MODE_ENABLED, true, errors);
      spectatorModeTime = get(values, SPECTATOR_MODE_TIME, 120, errors);
      team1Prefix = Util.convertColors(get(values, PREFIX_TEAM1, "&c", errors));
      team2Prefix = Util.convertColors(get(values, PREFIX_TEAM2, "&3", errors));
      groundHeight = getGroundHeightValue(values, GROUND_HEIGHT, errors);
      groundDamage = get(values, GROUND_DAMAGE, 4, errors);
      groundSchematic = get(values, GROUND_SCHEMATIC, null, errors);
      arenaRegion = getRegion(values, REGIONS_ARENA_MIN, REGIONS_ARENA_MAX, errors);
      innerRegion = getRegion(values, REGIONS_INNER_MIN, REGIONS_INNER_MAX, errors);
      team1Region = getRegion(values, REGIONS_TEAM1_MIN, REGIONS_TEAM1_MAX, errors);
      team2Region = getRegion(values, REGIONS_TEAM2_MIN, REGIONS_TEAM2_MAX, errors);
      team1Warp = getLocation(values, FIGHT_START_TEAM1, errors);
      team2Warp = getLocation(values, FIGHT_START_TEAM2, errors);
      spawnWarp = getLocation(values, SPAWN, errors);

      if (!errors.hasErrors()) {
        this.team1Warp = Util.lookAt(this.team1Warp, this.team2Warp);
        this.team2Warp = Util.lookAt(this.team2Warp, this.team1Warp);
      }
    }

    private <T extends Object> T get(Map<String, Object> values, String key, T defaultValue,
        ErrorMessages errors) {
      try {
        if (values.containsKey(key)) {
          return (T) values.get(key);
        }
      } catch (Exception ignored) {
      }
      errors.addWarning("Could not load " + key + " from configuration. Falling back to default of "
          + defaultValue);
      return defaultValue;
    }

    private WgRegion getRegion(Map<String, Object> values, String keyMin, String keyMax,
        ErrorMessages errors) {
      Location min = getLocation(values, keyMin, errors);
      Location max = getLocation(values, keyMax, errors);
      if (min == null || max == null) {
        errors.addError(
            "Region is defined wrongly! Look at the above for errors related to locations.");
        return null;
      }
      return WgRegion.from(this.getWorld(), min, max);
    }

    private Location getLocation(Map<String, Object> values, String key, ErrorMessages errors) {
      if (worldName == null) {
        return null;
      }
      String location = get(values, key, null, errors);
      if (location == null) {
        return null;
      }
      String[] splited = location.split(";");
      if (splited.length != 3) {
        errors.addError("Format of location wrong " + location);
        return null;
      }
      try {
        return new Location(this.getWorld(), Double.parseDouble(splited[0]),
            Double.parseDouble(splited[1]), Double.parseDouble(splited[2]));
      } catch (Exception ex) {
        errors.addError("Location can't contain characters " + location);
        return null;
      }
    }

    private String getWorldName(Map<String, Object> values, String key, ErrorMessages errors) {
      String worldName = get(values, key, null, errors);
      if (worldName == null) {
        return null;
      }
      if (!this.existsWorld(worldName)) {
        errors.addError("World '" + worldName + "' does not exist");
        return null;
      }
      return worldName;
    }

    private int getGroundHeightValue(Map<String, Object> values, String key, ErrorMessages errors) {
      int groundHeight = get(values, key, -1, errors);
      if (this.worldName == null || groundHeight < 0 || groundHeight > this.getWorld()
          .getMaxHeight()) {
        errors.addError("Ground height needs to be within world boundaries.");
        return -1;
      }
      return groundHeight;
    }

    private boolean existsWorld(String name) {
      if (name == null) {
        return false;
      }
      return Bukkit.getWorld(name) != null;
    }

    public World getWorld() {
      return Bukkit.getWorld(worldName);
    }

    public void setWorld(String worldName) {
      if (this.existsWorld(worldName)) {
        this.worldName = worldName;
      }
    }

    public void setArenaRegion(WgRegion region) {
      if (region != null) {
        this.arenaRegion = region;
      }
    }

    public void setInnerRegion(WgRegion region) {
      if (region != null) {
        this.innerRegion = region;
      }
    }

    public void setTeam1Region(WgRegion region) {
      if (region != null) {
        this.team1Region = region;
      }
    }

    public void setTeam2Region(WgRegion region) {
      if (region != null) {
        this.team2Region = region;
      }
    }

    public void setGroundHeight(int height) {
      if (height >= 0 && height <= this.getWorld().getMaxHeight()) {
        this.groundHeight = height;
      }
    }

    private String serializeLocation(Location location) {
      return location.getX() + ";" + location.getY() + ";" + location.getZ();
    }

    private String serializePoint(Point point) {
      return point.getX() + ";" + point.getY() + ";" + point.getZ();
    }

    public Map<String, Object> serialize() {
      Map<String, Object> values = new HashMap<>();
      values.put(WORLD, this.worldName != null ? worldName : "");
      values.put(MODE, this.fightMode);
      values.put(AUTO_RESET, this.isAutoReset);
      values.put(WATER_REMOVE, this.waterRemove);
      values.put(FOOD_LEVEL_CHANGE, this.foodLevelChange);
      values.put(SCOREBOARD_ENABLED, this.isScoreboardEnabled);
      values.put(SCOREBOARD_TIME, this.scoreboardTime);
      values.put(SPECTATOR_MODE_ENABLED, this.isSpectatorModeEnabled);
      values.put(SPECTATOR_MODE_TIME, this.spectatorModeTime);
      values.put(PREFIX_TEAM1, this.team1Prefix);
      values.put(PREFIX_TEAM2, this.team2Prefix);
      values.put(GROUND_HEIGHT, this.groundHeight);
      values.put(GROUND_DAMAGE, this.groundDamage);
      values.put(GROUND_SCHEMATIC, this.groundSchematic);
      values.put(REGIONS_ARENA_MIN, serializePoint(this.arenaRegion.getMin()));
      values.put(REGIONS_ARENA_MAX, serializePoint(this.arenaRegion.getMax()));
      values.put(REGIONS_INNER_MIN, serializePoint(this.innerRegion.getMin()));
      values.put(REGIONS_INNER_MAX, serializePoint(this.innerRegion.getMax()));
      values.put(REGIONS_TEAM1_MIN, serializePoint(this.team1Region.getMin()));
      values.put(REGIONS_TEAM1_MAX, serializePoint(this.team1Region.getMax()));
      values.put(REGIONS_TEAM2_MIN, serializePoint(this.team2Region.getMin()));
      values.put(REGIONS_TEAM2_MAX, serializePoint(this.team2Region.getMax()));
      values.put(FIGHT_START_TEAM1, serializeLocation(team1Warp));
      values.put(FIGHT_START_TEAM2, serializeLocation(team2Warp));
      values.put(SPAWN, serializeLocation(spawnWarp));
      return values;
    }
  }
}
