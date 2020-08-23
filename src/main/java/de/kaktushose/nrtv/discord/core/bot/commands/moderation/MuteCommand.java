package de.kaktushose.nrtv.discord.core.bot.commands.moderation;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;


@Permissions(PermissionLevel.MODERATOR)
public class MuteCommand extends Command {

    private Bot bot;

    public MuteCommand(Bot bot) {
        this.bot = bot;
    }

    @Override

    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Syntaxfehler")
                .addField("Syntax",
                        "`" + bot.getPrefix() + "mute <@Member|#Channel>`",
                        true).build()).queue();
    }

    @SubCommand(MEMBER_MENTION)
    public void muteMember(Member executor, Arguments args, TextChannel channel, Message message) {
        Member member = args.getAsMember(0);
        BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());

        if (member.getIdLong() == 393843637437464588L) return;
        if (botUser.getPermissionLevel() == 0) {
            botUser.setPermissionLevel(1);
            channel.sendMessage(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setDescription(member.getAsMention() + " wird nun nicht mehr vom Bot ignoriert!")
                    .addField("Hinweis", "Das Berechtigungslevel wurde wieder auf 1 gesetzt. Nutze `" +
                            bot.getPrefix() + "setperms @Member` um das Level zu erhöhen.", false)
                    .build()).queue();
        } else {
            botUser.setPermissionLevel(0);
            channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN)
                    .setDescription(member.getAsMention() + " wird nun vom Bot ignoriert!")
                    .build()).queue();
        }
        bot.getDatabase().setBotUser(botUser);
    }

    @SubCommand(CHANNEL_MENTION)
    public void muteChannel(Member executor, Arguments args, TextChannel channel, Message message) {
        TextChannel textChannel = args.getAsChannel(0);
        if (bot.getDatabase().getMutedChannelIds().contains(textChannel.getIdLong())) {
            bot.getDatabase().removeMutedChannel(textChannel.getIdLong());
            channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN)
                    .setDescription(channel.getAsMention() + " wird nun nicht mehr vom Bot ignoriert!")
                    .build()).queue();
        } else {
            channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN)
                    .setDescription(channel.getAsMention() + " wird nun vom Bot ignoriert!")
                    .build()).queue();
            bot.getDatabase().addMutedChannel(textChannel.getIdLong());
        }
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" +  bot.getPrefix() + "add <coins|xp> <@Member|@Rolle|all> <amount>`")
                .addField("Beschreibung:", "Mutet einen User beim Bot, wodurch er keine Commands mehr ausführen kann und Nachrichten nicht mehr gewertet werden.\n" +
                        "Dieser Command wird ebenfalls genutzt, um einen User wieder zu entmuten.", false)
                .addField("Berechtigungslevel", PermissionLevel.MODERATOR.name(), false)
                .addField("seit Version", "2.1.0", false)
                .build()).queue();
    }
}
