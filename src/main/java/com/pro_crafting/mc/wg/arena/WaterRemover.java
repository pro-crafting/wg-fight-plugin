package com.pro_crafting.mc.wg.arena;

import com.pro_crafting.mc.wg.WarGear;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class WaterRemover implements Listener {

  private Arena arena;
  private WarGear plugin;
  private List<SimpleEntry<Location, Integer>> explodedBlocks;
  private List<Block> waterList;
  private BukkitTask task;

  public WaterRemover(WarGear plugin, Arena arena) {
    this.arena = arena;
    this.plugin = plugin;
    explodedBlocks = new ArrayList<>();
    waterList = new ArrayList<>();
  }

  public void start() {
    stop();
    if (this.arena.getRepo().isWaterRemove()) {
      this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
      explodedBlocks = new ArrayList<>();
      waterList = new ArrayList<>();
      task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable() {
        public void run() {
          wateredCheck();
          removeWater();
        }
      }, 0, 20);
    }
  }

  public void stop() {
    if (task != null) {
      task.cancel();
    }
    HandlerList.unregisterAll(this);
  }

  public void add(Location loc) {
    this.explodedBlocks.add(new SimpleEntry<>(loc, 0));
  }

  private void wateredCheck() {
    for (int i = this.explodedBlocks.size() - 1; i > -1; i--) {
      if (this.explodedBlocks.get(i).getValue() >= 15) {
        Block b = this.explodedBlocks.get(i).getKey().getBlock();
        if (b.getType() == Material.WATER || b.getBlockData() instanceof Waterlogged) {
          this.waterList.add(b);
        }
        this.explodedBlocks.remove(i);
      } else {
        this.explodedBlocks.get(i).setValue(this.explodedBlocks.get(i).getValue() + 1);
      }
    }
  }

  private void removeWater() {
    for (int i = this.waterList.size() - 1; i > -1; i--) {
      Block current = this.waterList.get(i);
      for (Block removeBlock : getSourceBlocksOfWater(current)) {
        removeBlock.setType(Material.AIR);
      }
      if (current.getType() == Material.AIR) {
        this.waterList.remove(i);
      }
    }
  }

  private List<Block> getSourceBlocksOfWater(Block startBlock) {
    List<Block> water = new ArrayList<>();
    collectBlocks(startBlock, water, new ArrayList<>());
    return water;
  }


  /* code by: andf54
   * https://forums.bukkit.org/threads/get-the-whole-stream-of-water-or-lava.110156/
   * Einige kleinere �nderungen vorgenommen
   */
  public void collectBlocks(Block anchor, List<Block> collected, List<Block> visitedBlocks) {
    if (!(anchor.getType() == Material.WATER || anchor.getType() == Material.WATER)) {
      return;
    }

    if (visitedBlocks.contains(anchor)) {
      return;
    }
    visitedBlocks.add(anchor);
    if (anchor.getType() == Material.WATER) {
      collected.add(anchor);
    }

    collectBlocks(anchor.getRelative(BlockFace.UP), collected, visitedBlocks);
    collectBlocks(anchor.getRelative(BlockFace.NORTH), collected, visitedBlocks);
    collectBlocks(anchor.getRelative(BlockFace.EAST), collected, visitedBlocks);
    collectBlocks(anchor.getRelative(BlockFace.SOUTH), collected, visitedBlocks);
    collectBlocks(anchor.getRelative(BlockFace.WEST), collected, visitedBlocks);
  }
}
