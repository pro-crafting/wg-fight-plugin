package de.pro_crafting.kit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.pro_crafting.kit.plugins.AdminCmdKit;
import de.pro_crafting.kit.plugins.EssentialsKit;

public class KitAPI 
{
	private List<KitPlugin> kitPlugins;
	
	public KitAPI()
	{
		this.kitPlugins = new ArrayList<KitPlugin>();
		this.loadKitPlugins();
	}
	
	private void loadKitPlugins()
	{
		hookKitPlugin("AdminCmd", AdminCmdKit.class);
		hookKitPlugin("Essentials", EssentialsKit.class);
	}
	
	private void hookKitPlugin(String name, Class<? extends KitPlugin> hookClass)
	{
		if (Bukkit.getPluginManager().getPlugin(name) != null)
		{
			try {
				this.kitPlugins.add(hookClass.getConstructor().newInstance());
			} catch (Exception ex) {
				
			}
		}
	}
	
	public boolean existsKit(String kitName)
	{
		boolean exists = false;
		for (KitPlugin curr : this.kitPlugins)
		{
			if (!exists)
			{
				exists = curr.existsKit(kitName);
			}
		}
		return exists;
	}
	
	public void giveKit(String kitName, Player p)
	{
		for (KitPlugin curr : this.kitPlugins)
		{
			if (curr.existsKit(kitName))
			{
				curr.giveKit(kitName, p);
				return;
			}
		}
	}
	
	public ItemStack[] getKitItems(String kitName)
	{
		for (KitPlugin curr : this.kitPlugins)
		{
			if (curr.existsKit(kitName))
			{
				return curr.getKitItems(kitName);
			}
		}
		return new ItemStack[0];
	}
}
