package de.pro_crafting.kit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.pro_crafting.kit.plugins.AdminCmdProvider;
import de.pro_crafting.kit.plugins.CommandBookProvider;
import de.pro_crafting.kit.plugins.EssentialsProvider;

public class KitAPI 
{
	private List<KitProvider> kitProviders;
	
	public KitAPI()
	{
		this.kitProviders = new ArrayList<KitProvider>();
		this.loadKitPlugins();
	}
	
	private void loadKitPlugins()
	{
		hookKitPlugin("AdminCmd", AdminCmdProvider.class);
		hookKitPlugin("Essentials", EssentialsProvider.class);
		hookKitPlugin("CommandBook", CommandBookProvider.class);
	}
	
	private void hookKitPlugin(String name, Class<? extends KitProvider> hookClass)
	{
		if (Bukkit.getPluginManager().getPlugin(name) != null)
		{
			try {
				this.kitProviders.add(hookClass.getConstructor().newInstance());
			} catch (Exception ex) {
				
			}
		}
	}
	
	public boolean existsKit(String kitName)
	{
		boolean exists = false;
		for (KitProvider curr : this.kitProviders)
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
		for (KitProvider curr : this.kitProviders)
		{
			if (curr.existsKit(kitName))
			{
				curr.distribute(kitName, p);
				return;
			}
		}
	}
	
	public ItemStack[] getKitItems(String kitName)
	{
		for (KitProvider curr : this.kitProviders)
		{
			if (curr.existsKit(kitName))
			{
				return curr.getItems(kitName);
			}
		}
		return new ItemStack[0];
	}
}
