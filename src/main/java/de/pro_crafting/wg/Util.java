package de.pro_crafting.wg;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import de.pro_crafting.wg.arena.Arena;

public class Util {
	public static Arena getArenaFromSender(WarGear plugin, CommandSender sender, String[] args)
	{
		boolean hasFoundArenaFlag = false;
		Arena ret = null;
		for (String argument : args)
		{
			if (hasFoundArenaFlag)
			{
				ret = plugin.getArenaManager().getArena(argument);
			}
			hasFoundArenaFlag = argument.equalsIgnoreCase("-a");
		}
		if (ret != null)
		{
			return ret;
		}
		if (!(sender instanceof ConsoleCommandSender))
		{
			if (sender instanceof Player)
			{
				ret = plugin.getArenaManager().getArenaAt(((Player)sender).getLocation());
			}
			else if (sender instanceof BlockCommandSender)
			{
				ret = plugin.getArenaManager().getArenaAt(((BlockCommandSender)sender).getBlock().getLocation());
			}
		}
		return ret;
	}
	
	public static String[] removeFlagsFromArgs(String[] args)
	{
		List<String> ret = new ArrayList<String>();
		boolean removeNextArg = false;
		for (String argument : args)
		{
			if (argument.equals("-a"))
			{
				removeNextArg = true;
				continue;
			}
			if (removeNextArg)
			{
				removeNextArg = false;
				continue;
			}
			ret.add(argument);
		}
		return ret.toArray(new String[0]);
	}
	
	public static void makeHealthy(Player player)
	{
		feed(player);
		heal(player);
		player.setFireTicks(0);
	}
	
	public static void feed(Player player)
	{
		player.setFoodLevel(20);
		player.setSaturation(10);
	}
	
	public static void heal(Player player)
	{
		player.setHealth(player.getMaxHealth());
	}
	
	public static void disableFly(Player player)
	{
		player.setAllowFlight(false);
		player.setFlying(false);
	}
	
	public static void enableFly(Player player)
	{
		player.setAllowFlight(true);
		player.setFlying(true);
	}
	
	public static Location move(Location start, int distance)
	{
		Location ret = start.clone();
		ret.add(ret.getDirection().normalize().multiply(distance).toLocation(start.getWorld()));
		return ret;
	}
	
	public static void removePotionEffects(Player player)
	{
		for (PotionEffect effect : player.getActivePotionEffects())
		{
			player.getPlayer().removePotionEffect(effect.getType());
		}
	}
	
	//Lookat function of bergerkiller
	//https://forums.bukkit.org/threads/lookat-and-move-functions.26768/
	public static Location lookAt(Location loc, Location lookat) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();

        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();

        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }

        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        // Set pitch
        loc.setPitch((float) -Math.atan(dy / dxz));

        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

        return loc;
    }
}
