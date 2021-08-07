package net.essentialsx.discordlink.commands.discord;

import com.google.common.collect.ImmutableList;
import net.essentialsx.api.v2.services.discord.InteractionCommand;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgument;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgumentType;
import net.essentialsx.api.v2.services.discord.InteractionEvent;
import net.essentialsx.api.v2.services.discord.InteractionRole;

import java.util.List;

import static com.earth2me.essentials.I18n.tl;

public class RoleInfoCommand implements InteractionCommand {
    @Override
    public void onCommand(InteractionEvent event) {
        final InteractionRole role = event.getRoleArgument("role");
        event.reply(tl("discordCommandRoleInfoArgumentResponse", role.getAsMention(), role.getId()));
    }

    @Override
    public String getName() {
        return "roleinfo";
    }

    @Override
    public String getDescription() {
        return tl("discordCommandRoleInfoDescription");
    }

    @Override
    public List<InteractionCommandArgument> getArguments() {
        return ImmutableList.of(new InteractionCommandArgument("role", tl("discordCommandRoleInfoArgumentRole"), InteractionCommandArgumentType.ROLE, true));
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}
