package de.pro_crafting.wg.commands.provider;


import com.google.common.collect.ImmutableList;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.Nullable;

public class CommandSenderProvider implements Provider<CommandSender> {
    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public CommandSender get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        return arguments.getNamespace().get(CommandSender.class);
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return ImmutableList.of();
    }
}
