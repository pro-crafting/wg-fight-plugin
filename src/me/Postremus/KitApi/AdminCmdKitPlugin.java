package me.Postremus.KitApi;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.Balor.Kit.KitInstance;
import be.Balor.bukkit.AdminCmd.ACHelper;

public class AdminCmdKitPlugin implements IKitPlugin
{
	Server server;
	
	@Override
	public void setServer(Server server)
	{
		this.server = server;
	}

	@Override
	public boolean existsKit(String kitName) {
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
		return kit != null;
	}

	@Override
	public void giveKit(String kitName, Player p) {
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
	    ItemStack[] items = kit.getItemStacks().toArray(new ItemStack[] {});
	    p.getInventory().addItem(items);
	}
}
