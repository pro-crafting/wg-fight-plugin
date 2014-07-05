package de.pro_crafting.wg.modes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import de.pro_crafting.wg.FightMode;
import de.pro_crafting.wg.Util;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;

public class ChestMode extends FightBase implements FightMode, Listener{

	private BukkitTask task;
	private int counter;
	private boolean areChestsOpen;
	
	public ChestMode(WarGear plugin, Arena arena)
	{
		super(plugin, arena);
	}
	
	@Override
	public void start() {
		super.start();
		this.fillChest(this.arena.getRepo().getTeam1Warp());
		this.fillChest(this.arena.getRepo().getTeam2Warp());
		
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
		PlayerMoveEvent.getHandlerList().unregister(this);
		
		counter = 0;
		task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable(){
			public void run()
			{
				chestOpenCountdown();
			}
		}, 0, 20);
	}

	private void fillChest(Location loc)
	{
		Location l = loc.clone();
		l.setY(l.getY()-1);
		Location chestOne = Util.move(l, 2);
		Location chestTwo = Util.move(chestOne, 1);
		setChests(chestOne, chestTwo);
		
		ItemStack[] items = removeTNTStacks(this.plugin.getKitApi().getKitItems(this.arena.getKit()));
		Inventory chestOneInv = ((Chest)chestOne.getBlock().getState()).getBlockInventory();
		chestOneInv.setContents(items);
		
		fillChestWithTnt(chestOne);
		fillChestWithTnt(chestTwo);
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
	
	private void fillChestWithTnt(Location loc)
	{
		if (loc.getBlock().getType() != Material.CHEST)
		{
			return;
		}
		Inventory inv = ((Chest)loc.getBlock().getState()).getBlockInventory();
		for (int i=0;i<inv.getSize();i++)
		{
			if (inv.getItem(i) == null)
			{
				inv.setItem(i, new ItemStack(Material.TNT, 64));
			}
		}
	}
	
	private void setChests(Location chestOne, Location chestTwo)
	{
		chestOne.getBlock().setType(Material.CHEST);
		chestTwo.getBlock().setType(Material.CHEST);
	}
	
	private void chestOpenCountdown()
	{
		if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.GOLD+"Kisten werden ge§ffnet in:");
			this.arena.broadcastMessage(ChatColor.GOLD + "5 Sekunden");
		}
		else if (counter > 0 && counter < 2)
		{
			int diff = 5 - counter;
			this.arena.broadcastMessage(ChatColor.GOLD + ""+diff+" Sekunden");
		}
		else if (counter > 1 && counter < 5)
		{
			int diff = 5 - counter;
			this.arena.broadcastMessage(ChatColor.AQUA + ""+diff+" Sekunden");
		}
		else if (counter == 5)
		{
			counter = 0;
			areChestsOpen = true;
			this.arena.broadcastMessage(ChatColor.AQUA + "Kisten geöffnet!");
			task.cancel();
			task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable(){
				public void run()
				{
					fightPreStartCountdown();
				}
			}, 0, 20);
			return;
		}
		counter++;
	}
	
	private void fightPreStartCountdown()
	{
		if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.GOLD+"Kisten werden geschlossen in:");
			this.arena.broadcastMessage(ChatColor.GOLD + "30 Sekunden");
		}
		else if (counter == 10)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "20 Sekunden");
		}
		else if (counter == 15)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "15 Sekunden");
		}
		else if (counter == 20)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "10 Sekunden");
		}
		else if (counter > 24 && counter < 27)
		{
			int diff = 30 - counter;
			this.arena.broadcastMessage(ChatColor.GOLD + ""+diff+" Sekunden");
		}
		else if (counter > 26 && counter < 30)
		{
			int diff = 30 - counter;
			this.arena.broadcastMessage(ChatColor.AQUA + ""+diff+" Sekunden");
		}
		else if (counter == 30)
		{
			counter = 0;
			areChestsOpen = false;
			this.arena.broadcastMessage(ChatColor.AQUA + "Kisten geschlossen!");
			this.arena.broadcastMessage(ChatColor.AQUA + "Wargear betreten!");
			task.cancel();
			task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable(){
				public void run()
				{
					fightStartCountdown();
				}
			}, 0, 20);
			return;
		}
		counter++;
	}
	
	private void fightStartCountdown()
	{
		if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.GOLD+"Fight beginnt in:");
			this.arena.broadcastMessage(ChatColor.GOLD + "30 Sekunden");
		}
		else if (counter == 10)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "20 Sekunden");
		}
		else if (counter == 15)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "15 Sekunden");
		}
		else if (counter == 20)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "10 Sekunden");
		}
		else if (counter > 24 && counter < 27)
		{
			int diff = 30 - counter;
			this.arena.broadcastMessage(ChatColor.GOLD + ""+diff+" Sekunden");
		}
		else if (counter > 26 && counter < 30)
		{
			int diff = 30 - counter;
			this.arena.broadcastMessage(ChatColor.AQUA + ""+diff+" Sekunden");
		}
		else if (counter == 30)
		{
			task.cancel();
			PlayerInteractEvent.getHandlerList().unregister(this);
			this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
			this.arena.updateState(State.Running);
			arena.open();
			return;
		}
		counter++;
	}
	
	@Override
	public void stop() {
		super.stop();
		task.cancel();
		PlayerInteractEvent.getHandlerList().unregister(this);
	}

	@Override
	public String getName() {
		return "chest";
	}
	
	@EventHandler (priority=EventPriority.HIGHEST)
	public void playerInteractHandler(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return;
		}
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
		Location clicked = event.getClickedBlock().getLocation();
		if (isItemChestLocation(clicked, this.arena.getRepo().getTeam1Warp().clone()) ||
				isItemChestLocation(clicked, this.arena.getRepo().getTeam2Warp().clone()))
		{
			event.setCancelled(!areChestsOpen);
		}
	}
	
	private Boolean isItemChestLocation(Location value, Location checkAgainst)
	{
		Location l = checkAgainst.clone();
		l.setY(l.getY()-1);
		Location chestOne = Util.move(l, 2);
		Location chestTwo = Util.move(chestOne, 1);
		return equalsLocation(chestOne, value) || equalsLocation(chestTwo, value);
	}
	
	private Boolean equalsLocation(Location one, Location two)
	{
		return one.getBlockX() == two.getBlockX() && one.getBlockY() == two.getBlockY() && one.getBlockZ() == two.getBlockZ();
	}
}
