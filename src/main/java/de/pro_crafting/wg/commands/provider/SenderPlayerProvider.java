package de.pro_crafting.wg.commands.provider;

import com.google.common.collect.ImmutableList;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import de.pro_crafting.wg.commands.WarGearException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.Nullable;

public class SenderPlayerProvider implements Provider<Player> {
    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public Player get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        CommandSender sender = arguments.getNamespace().get(CommandSender.class);
        if (sender instanceof Player) {
            return (Player) sender;
        }
        throw new WarGearException("§cDer Command muss von einem Spieler ausgeführt werden.");
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return ImmutableList.of();
    }
}
