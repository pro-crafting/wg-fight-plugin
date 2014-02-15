package me.Postremus.WarGear.FightModes;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.Postremus.KitApi.KitAPI;
import me.Postremus.WarGear.IFightMode;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;

import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Chest;

public class ChestMode extends FightBase implements IFightMode, Listener{

	private Timer timer;
	private int counter;
	private boolean areChestsOpen;
	
	public ChestMode(WarGear plugin, Arena arena)
	{
		super(plugin, arena);
		timer = new Timer();
		areChestsOpen = false;
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		super.start();
		this.fillChest(this.arena.getRepo().getTeam1Warp());
		this.fillChest(this.arena.getRepo().getTeam1Warp());
		
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
		PlayerMoveEvent.getHandlerList().unregister(this);
		
		counter = 0;
		timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
	         public void run() {
				chestOpenCountdown();          
	         }
		}, 0, 1000);
	}

	private void fillChest(Location loc)
	{
		KitAPI kitapi = new KitAPI(this.plugin.getServer());
		loc.setY(loc.getY()-1); //Die kiste liegt unterhalb des spielers
		if (loc.getBlock().getType() != Material.CHEST)
		{
			loc.getBlock().setType(Material.CHEST);
			loc.setX(loc.getX()-1);
			loc.getBlock().setType(Material.CHEST);
			loc.setX(loc.getX()+1);
		}
		((Chest)loc.getBlock().getState()).getBlockInventory().clear();
		fillWithTnt(((Chest)loc.getBlock().getState()).getBlockInventory());
		loc.setX(loc.getX()-1);
		((Chest)loc.getBlock().getState()).getBlockInventory().clear();
		((Chest)loc.getBlock().getState()).getBlockInventory().setContents(removeTNTStacks(kitapi.getKitItems(this.arena.getKit())));
		fillWithTnt(((Chest)loc.getBlock().getState()).getBlockInventory());
	}
	
	private ItemStack[] removeTNTStacks(ItemStack[] withtnt)
	{
		List<ItemStack> ret = new ArrayList<ItemStack>();
		for (ItemStack stack : withtnt)
		{
			if (stack.getType() != Material.TNT)
			{
				ret.add(stack);
			}
		}
		return ret.toArray(new ItemStack[0]);
	}
	
	private void fillWithTnt(Inventory v)
	{
		for (int i=0;i<v.getSize();i++)
		{
			if (v.getItem(i) == null)
			{
				v.setItem(i, new ItemStack(Material.TNT, 64));
			}
		}
	}
	
	private void chestOpenCountdown()
	{
		if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.YELLOW+"Die Kisten werden geöffnet in:");
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "30 Sekunden");
		}
		else if (counter > 24 && counter < 30)
		{
			int diff = 30 - counter;
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + ""+diff+" Sekunden");
		}
		else if (counter == 30)
		{
			counter = 0;
			timer.cancel();
			timer = new Timer();
			this.areChestsOpen = true;
			this.arena.broadcastMessage(ChatColor.AQUA + "Kisten geöffnet!");
			timer.schedule(new TimerTask(){
				@Override
		         public void run() {
					fightPreStartCountdown();          
		         }
			}, 0, 1000);
			return;
		}
		counter++;
	}
	
	private void fightPreStartCountdown()
	{
		if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.YELLOW+"Wargears betreten in:");
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "30 Sekunden");
		}
		else if (counter > 24 && counter < 30)
		{
			int diff = 30 - counter;
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + ""+diff+" Sekunden");
		}
		else if (counter == 30)
		{
			counter = 0;
			timer.cancel();
			timer = new Timer();
			this.areChestsOpen = false;
			this.arena.broadcastMessage(ChatColor.AQUA + "Kisten geschlossen!");
			this.arena.broadcastMessage(ChatColor.AQUA + "Wargear betreten!");
			timer.schedule(new TimerTask(){
				@Override
		         public void run() {
					fightStartCountdown();          
		         }
			}, 0, 1000);
			return;
		}
		counter++;
	}
	
	private void fightStartCountdown()
	{
		if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.YELLOW+"Fight beginnt in:");
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "20 Sekunden");
		}
		else if (counter > 14 && counter < 20)
		{
			int diff = 20 - counter;
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + ""+diff+" Sekunden");
		}
		else if (counter == 20)
		{
			this.timer.cancel();
			this.arena.broadcastMessage(ChatColor.AQUA + "Fight beginnt. Viel Spaß. :)");
			this.arena.getRepo().getWorld().setDifficulty(Difficulty.EASY);
			PlayerInteractEvent.getHandlerList().unregister(this);
			this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
			this.arena.open();
			return;
		}
		counter++;
	}
	
	@Override
	public void stop() {
		super.stop();
		timer.cancel();
		PlayerMoveEvent.getHandlerList().unregister(this);
		PlayerInteractEvent.getHandlerList().unregister(this);
	}

	@Override
	public String getName() {
		return "chest";
	}
	
	@EventHandler
	public void playerInteractHandler(PlayerInteractEvent event)
	{
		if (event.getClickedBlock().getType() != Material.CHEST)
		{
			return;
		}
		if (event.getPlayer().hasPermission("wargear.chest.open"))
		{
			return;
		}
		
		if (this.arena.getTeam().getTeamOfPlayer(event.getPlayer()) == null)
		{
			event.setCancelled(true);
		}
		
		if (this.areChestsOpen)
		{
			return;
		}
		Chest b = ((Chest)event.getClickedBlock().getState());
		Location clickedChest = b.getLocation();
		if (compareChestLocation(clickedChest, arena.getRepo().getTeam1Warp()) || compareChestLocation(clickedChest, arena.getRepo().getTeam2Warp()))
		{
			event.setCancelled(this.areChestsOpen);
		}
	}
	
	private boolean compareChestLocation(Location loc, Location chestLoc)
	{
		if (!loc.getWorld().getName().equalsIgnoreCase(chestLoc.getWorld().getName()))
		{
			return false;
		}
		chestLoc.setY(chestLoc.getY()-1);
		if (loc.getBlockX() == chestLoc.getBlockX() && loc.getBlockY() == chestLoc.getBlockY() && loc.getBlockZ() == chestLoc.getBlockZ())
		{
			return true;
		}
		chestLoc.setZ(chestLoc.getZ()-1);
		if (loc.getBlockX() == chestLoc.getBlockX() && loc.getBlockY() == chestLoc.getBlockY() && loc.getBlockZ() == chestLoc.getBlockZ())
		{
			return true;
		}
		return false;
	}
}
