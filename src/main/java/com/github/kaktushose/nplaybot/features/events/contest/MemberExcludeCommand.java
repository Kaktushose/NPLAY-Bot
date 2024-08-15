package com.github.kaktushose.nplaybot.features.events.contest;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.ContextCommand;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interaction
@Permissions(BotPermissions.MODIFY_USER_BALANCE)
public class MemberExcludeCommand {

    private static final Logger log = LoggerFactory.getLogger(MemberExcludeCommand.class);

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @ContextCommand(value = "Vom Contest ausschließen", type = Command.Type.MESSAGE, isGuildOnly = true, ephemeral = true, enabledFor = Permission.BAN_MEMBERS)
    public void onRemoveContestPost(CommandEvent event, Message message) {
        log.info("Excluding member {} from contest event", message.getAuthor());
        database.getContestEventService().setVoteCount(message.getIdLong(), 0);
        message.delete().queue();
        event.getGuild().addRoleToMember(message.getAuthor(), message.getGuild().getRoleById(885530505192284210L)).queue();
        event.reply(embedCache.getEmbed("memberExcluded"));
    }

}
