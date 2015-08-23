package de.pro_crafting.kit.plugins;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.kits.KitsComponent;

import de.pro_crafting.kit.KitProvider;

public class CommandBookProvider implements KitProvider{

	public boolean existsKit(String kitName) {
		return CommandBook.inst().getComponentManager().getComponent(KitsComponent.class).getKitManager().getKit(kitName) != null;
	}

	public ItemStack[] getItems(String kitName) {
		return null;
	}

	public void distribute(String kitName, Player player) {
		if (existsKit(kitName)) {
			CommandBook.inst().getComponentManager().getComponent(KitsComponent.class).getKitManager().getKit(kitName).distribute(player);
		}
	}
	
	public String getName() {
		return "Commandbook";
	}
}
