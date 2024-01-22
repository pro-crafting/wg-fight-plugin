package com.pro_crafting.mc.wg.arena;

import com.sk89q.worldedit.extension.platform.Platform;

public enum ArenaPosition {
  Outside,
  Platform,
  /**
   * Inner Region, team1 side
   */
  Team1PlayField,
  /**
   * Inner Region, team2 side
   */
  Team2PlayField,

  /**
   * WG Region, only the part where the wg stands, team1 side
   */
  Team1WG,

  /**
   * WG Region, only the part where the wg stands, team1 side
   */
  Team2WG
}
