package com.pro_crafting.mc.wg.arena;

import com.pro_crafting.mc.blockgenerator.JobState;
import com.pro_crafting.mc.blockgenerator.JobStateChangedCallback;
import com.pro_crafting.mc.blockgenerator.criteria.CuboidCriteria;
import com.pro_crafting.mc.blockgenerator.job.Job;
import com.pro_crafting.mc.blockgenerator.job.SimpleJob;
import com.pro_crafting.mc.blockgenerator.provider.SchematicProvider;
import com.pro_crafting.mc.blockgenerator.provider.SingleBlockProvider;
import com.pro_crafting.mc.common.Point;
import com.pro_crafting.mc.common.Size;
import com.pro_crafting.mc.wg.group.GroupSide;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.event.ArenaStateChangeEvent;
import com.pro_crafting.mc.wg.model.WgRegion;
import java.io.File;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Reseter implements Listener, JobStateChangedCallback {

  private Arena arena;
  private WarGear plugin;
  private int groundHeight;

  public Reseter(WarGear plugin, Arena arena) {
    this.arena = arena;
    this.plugin = plugin;
    groundHeight = arena.getRepo().getGroundHeight();
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  public void reset() {
    WgRegion rg = this.arena.getRepo().getInnerRegion();
    World world = this.arena.getRepo().getWorld();
    Point origin = rg.getMin();
    origin.setY(groundHeight);
    Size size = new Size(rg.getWidth(), rg.getMax().getY() - groundHeight, rg.getLength());
    this.plugin.getGenerator().addJob(new SimpleJob(origin, size, world, this,
        new SingleBlockProvider(new CuboidCriteria(), Material.AIR.createBlockData()), true));
  }

  public void pasteGround(World arenaWorld) {
    WorldEdit we = this.plugin.getRepo().getWorldEdit().getWorldEdit();
    LocalConfiguration config = we.getConfiguration();

    File dir = we.getWorkingDirectoryFile(config.saveDir);
    String schemName = this.arena.getRepo().getGroundSchematic();
    File schematic = new File(dir, schemName);
    if (!schematic.exists()) {
      schemName += ".schem";
    }

    schematic = new File(dir, schemName);
    if (!schematic.exists()) {
      this.plugin.getLogger().info("Boden wird nicht gepasted. Schematic nicht gefunden.");
      return;
    }

    SchematicProvider provider = new SchematicProvider(new CuboidCriteria(), schematic);
    this.plugin.getGenerator()
        .addJob(new SimpleJob(provider.getOrigin(), arenaWorld, null, provider));
  }

  private void removeItems(World arenaWorld) {
    WgRegion rg = this.arena.getRepo().getInnerRegion();
    for (Entity curr : arenaWorld.getEntitiesByClasses(Item.class, Arrow.class)) {
      if (rg.contains(curr.getLocation())) {
        curr.remove();
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void arenaStateChangedHandler(ArenaStateChangeEvent event) {
    if (!event.getArena().equals(this.arena)) {
      return;
    }

    if (event.getTo() == State.PreRunning) {
      this.removeItems(this.arena.getRepo().getWorld());
    } else if (event.getTo() == State.Resetting) {
      this.reset();
    }
  }

  public void jobStateChanged(Job job, JobState fromState) {
    if (job.getState() != JobState.Finished) {
      return;
    }
    removeItems(this.arena.getRepo().getWorld());
    try {
      this.pasteGround(this.arena.getRepo().getWorld());
    } catch (Exception e) {
      this.plugin.getLogger()
          .info("Boden in " + this.arena.getName() + " konnte nicht geresetet werden.");
    }
    if (this.arena.getState() == State.Resetting) {
      this.arena.updateState(State.Idle);
    }
  }

  public void cleanSide(GroupSide side) {
    WgRegion rg = this.arena.getRepo().getTeamRegion(side);
    World world = this.arena.getRepo().getWorld();
    Point origin = rg.getMin();
    origin.setY(groundHeight);
    Size size = new Size(rg.getMax().getX() - rg.getMin().getX(),
        rg.getMax().getY() - rg.getMin().getY(), rg.getMax().getZ() - rg.getMin().getZ());
    this.plugin.getGenerator().addJob(new SimpleJob(origin, size, world, null,
        new SingleBlockProvider(new CuboidCriteria(), Material.AIR.createBlockData())));
  }
}
