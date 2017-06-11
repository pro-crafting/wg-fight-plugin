package com.pro_crafting.mc.wg.arena;

import com.pro_crafting.mc.wg.group.GroupSide;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import de.pro_crafting.common.Point;
import de.pro_crafting.common.Size;
import de.pro_crafting.generator.JobState;
import de.pro_crafting.generator.JobStateChangedCallback;
import de.pro_crafting.generator.criteria.CuboidCriteria;
import de.pro_crafting.generator.job.Job;
import de.pro_crafting.generator.job.SimpleJob;
import de.pro_crafting.generator.provider.SchematicProvider;
import de.pro_crafting.generator.provider.SingleBlockProvider;
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
    CuboidRegion rg = this.arena.getPlayGroundRegion();
    World world = this.arena.getRepo().getWorld();
    Point origin = new Point(BukkitUtil.toLocation(world, rg.getMinimumPoint()));
    origin.setY(groundHeight);
    Size size = new Size(rg.getWidth(), rg.getMaximumY() - groundHeight, rg.getLength());
    this.plugin.getGenerator().addJob(new SimpleJob(origin, size, world, this,
        new SingleBlockProvider(new CuboidCriteria(), Material.AIR, (byte) 0), true));
  }

  private void pasteGround(World arenaWorld) {
    WorldEdit we = this.plugin.getRepo().getWorldEdit().getWorldEdit();
    LocalConfiguration config = we.getConfiguration();

    File dir = we.getWorkingDirectoryFile(config.saveDir);
    String schemName = this.arena.getRepo().getGroundSchematic();
    if (!schemName.contains(".schematic")) {
      schemName = schemName + ".schematic";
    }
    File schematic = new File(dir, schemName);

    SchematicProvider provider = new SchematicProvider(new CuboidCriteria(), schematic);
    this.plugin.getGenerator()
        .addJob(new SimpleJob(provider.getOrigin(), arenaWorld, null, provider));
  }

  private void removeItems(World arenaWorld) {
    CuboidRegion rg = this.arena.getPlayGroundRegion();
    for (Entity curr : arenaWorld.getEntitiesByClasses(Item.class, Arrow.class)) {
      if (rg.contains(BukkitUtil.toVector(curr.getLocation()))) {
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
        new SingleBlockProvider(new CuboidCriteria(), Material.AIR, (byte) 0)));
  }
}
