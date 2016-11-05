package de.pro_crafting.wg.commands.provider;

import com.google.common.collect.ImmutableList;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import de.pro_crafting.wg.group.PlayerRole;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.Nullable;


public class PlayerRoleProvider implements Provider<PlayerRole> {
    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public PlayerRole get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        if (!arguments.hasNext()) {
            return null;
        }

        String teamString = arguments.next();
        PlayerRole teamName = PlayerRole.Team1;
        if (teamString.equalsIgnoreCase("team2")) {
            teamName = PlayerRole.Team2;
        }

        return teamName;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return ImmutableList.of();
    }
}
