package me.Postremus.WarGear;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.Balor.Kit.KitInstance;
import be.Balor.Tools.Warp;
import be.Balor.World.ACWorld;
import be.Balor.bukkit.AdminCmd.ACHelper;

public class AdmincmdWrapper {

	public static void teleportToWarp(Player player, String warpPoint, String worldName)
	{
		System.out.print("World: "+worldName);
		System.out.print("Warp: "+warpPoint);
		ACWorld world = ACWorld.getWorld(worldName);
		Warp warpPointac = world.getWarp(warpPoint);
		player.teleport(warpPointac.loc);
	}
	
	public static Location getWarpLocation(String warpPoint, String worldName)
	{
		ACWorld world = ACWorld.getWorld(worldName);
		Warp warpPointac = world.getWarp(warpPoint);
		return warpPointac.loc;
	}
	
	public static Boolean existsKit(String kitName)
	{
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
		return kit != null;
	}
	
	public static void giveKit(String kitName, Player player)
	{
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
	    ItemStack[] items = kit.getItemStacks().toArray(new ItemStack[] {});
	    player.getInventory().addItem(items);
	}
	
	public static void heal(Player player)
	{
		player.setHealth(player.getMaxHealth());
		player.setFireTicks(0);
		player.setFoodLevel(20);
	}
	
	public static void disableFly(Player player)
	{
		player.setAllowFlight(false);
		player.setFlying(false);
	}
}
