package me.Postremus.KitApi;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.Balor.Kit.KitInstance;
import be.Balor.bukkit.AdminCmd.ACHelper;

public class AdminCmdKitPlugin implements KitPlugin
{
	@Override
	public boolean existsKit(String kitName) {
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
		return kit != null;
	}

	@Override
	public void giveKit(String kitName, Player p) {
	    p.getInventory().addItem(getKitItems(kitName));
	}

	@Override
	public ItemStack[] getKitItems(String kitName) {
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
		return kit.getItemStacks().toArray(new ItemStack[] {});
	}
}
