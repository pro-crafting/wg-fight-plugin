package me.Postremus.WarGear.FightModes;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.Postremus.KitApi.KitAPI;
import me.Postremus.WarGear.FightState;
import me.Postremus.WarGear.IFightMode;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;

import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Chest;

public class ChestMode extends FightBase implements IFightMode, Listener{

	private Timer timer;
	private int counter;
	
	public ChestMode(WarGear plugin, Arena arena)
	{
		super(plugin, arena);
		timer = new Timer();
	}
	
	@Override
	public void start() {
		super.start();
		this.fillChest(this.arena.getRepo().getTeam1Warp());
		this.fillChest(this.arena.getRepo().getTeam2Warp());
		
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
		loc.setY(loc.getY()-1);
		Location chestOne = loc.add(0, 0, 2);
		Location chestTwo = chestOne.add(0, 0, 1);
		setChests(chestOne, chestTwo);
		
		ItemStack[] items = removeTNTStacks(this.plugin.getKitApi().getKitItems(this.arena.getKit()));
		Inventory chestOneInv = ((Chest)loc.getBlock().getState()).getBlockInventory();
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
			this.arena.updateFightState(FightState.Running);
			arena.open();
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
		
		if (!isItemChestLocation(event.getClickedBlock().getLocation(), this.arena.getRepo().getTeam1Warp()) &&
				!isItemChestLocation(event.getClickedBlock().getLocation(), this.arena.getRepo().getTeam2Warp()))
		{
			return;
		}
		event.setCancelled(this.arena.getFightState() != FightState.PreRunning);
	}
	
	private Boolean isItemChestLocation(Location value, Location checkAgainst)
	{
		checkAgainst.setY(checkAgainst.getY()-1);
		Location chestOne = checkAgainst.add(0, 0, 2);
		Location chestTwo = chestOne.add(0, 0, 1);
		
		return chestOne.equals(value) || chestTwo.equals(value);
	}
}
