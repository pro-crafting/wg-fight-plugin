package de.pro_crafting.wg.model;

import de.pro_crafting.common.Point;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;

@Data
@NoArgsConstructor
public class WgRegion {

  private World world;
  private Point min;
  private Point max;

  public static WgRegion from(World world, Location min, Location max) {
    WgRegion ret = new WgRegion();
    ret.setWorld(world);
    ret.setMin(new Point(min));
    ret.setMax(new Point(max));

    return ret;
  }

  public boolean contains(Location location) {
    if (!this.getWorld().equals(location.getWorld())) {
      return false;
    }
    if (this.getMin().getX() > location.getX() || this.getMin().getY() > location.getY()
        || this.getMin().getZ() > location.getZ()) {
      return false;
    }
    if (this.getMax().getX() < location.getX() || this.getMax().getY() < location.getY()
        || this.getMax().getZ() < location.getZ()) {
      return false;
    }
    return true;
  }
}
