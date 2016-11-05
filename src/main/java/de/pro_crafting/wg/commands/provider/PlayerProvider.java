package de.pro_crafting.wg.commands.provider;

import com.google.common.collect.ImmutableList;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import de.pro_crafting.wg.commands.WarGearException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.Nullable;

public class PlayerProvider implements Provider<Player> {
    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public Player get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        if (!arguments.hasNext()) {
            throw new WarGearException("§cDu musst einen Spieler angeben.");
        }
        String playerName = arguments.next();
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            throw new WarGearException("§c" + playerName + " ist kein Spieler.");
        }
        return player;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return ImmutableList.of();
    }
}
