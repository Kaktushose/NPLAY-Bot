package com.github.kaktushose.nplaybot.events.contest;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.CommandConfig;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import com.google.inject.Inject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interaction
@Permissions(BotPermissions.MODIFY_USER_BALANCE)
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
public class MemberExcludeCommand {

    private static final Logger log = LoggerFactory.getLogger(MemberExcludeCommand.class);

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(value = "Vom Contest ausschließen", type = Type.MESSAGE)
    public void onRemoveContestPost(CommandEvent event, Message message) {
        log.info("Excluding member {} from contest event", message.getAuthor());
        database.getContestEventService().setVoteCount(message.getIdLong(), 0);
        message.delete().queue();
        event.getGuild().addRoleToMember(message.getAuthor(), message.getGuild().getRoleById(885530505192284210L)).queue();
        event.with().ephemeral(true).reply(embedCache.getEmbed("memberExcluded"));
    }

}
