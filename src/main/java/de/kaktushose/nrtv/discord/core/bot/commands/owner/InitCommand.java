package de.kaktushose.nrtv.discord.core.bot.commands.owner;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import de.kaktushose.nrtv.discord.frameworks.command.Permissions;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.EmoteType;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.ReactionWaiter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

@Permissions(PermissionLevel.BOTOWNER)
public class InitCommand extends Command {

    private Bot bot;

    public InitCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage("`Bist du sicher, was du da machst? Dies wird die gesamte Datenbank neu aufsetzten! Dadurch gehen alle Daten verloren!`").queue(msg -> {
            msg.addReaction(EmoteType.THUMBSUP.name).queue();
            msg.addReaction(EmoteType.THUMBSDOWN.name).queue();
            ReactionWaiter waiter = new ReactionWaiter(executor.getIdLong(), msg.getIdLong(), EmoteType.THUMBSUP, EmoteType.THUMBSDOWN);
            waiter.startWaiting();
            waiter.onEvent(event -> {
                if (event.getEmote().equals(EmoteType.THUMBSUP)) {
                    executor.getGuild().getMembers().forEach(member -> {
                        bot.getDatabase().removeBotUser(member.getIdLong());
                        bot.removeRoleByLevel(bot.getDatabase().getBotUser(member.getIdLong()).getLevel(), member);
                    });
                    bot.getDatabase().updateUserSet(executor.getGuild(), bot);
                    BotUser botUser = bot.getDatabase().getBotUser(393843637437464588L);
                    botUser.setPermissionLevel(4);
                    bot.getDatabase().setBotUser(botUser);
                    msg.delete().queue();
                    channel.sendMessage("`Datenbank neu aufgesetzt!`").queue();
                } else {
                    msg.delete().queue();
                    message.delete().queue();
                }
                waiter.stopWaiting();
            });
        });


    }
    
}
