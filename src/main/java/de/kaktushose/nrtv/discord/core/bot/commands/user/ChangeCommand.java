package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import de.kaktushose.nrtv.discord.frameworks.command.Permissions;
import de.kaktushose.nrtv.discord.frameworks.level.shop.DiamondExchanger;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.EmoteType;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.ReactionWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

@Permissions(PermissionLevel.MEMBER)
public class ChangeCommand extends Command {

    private final Bot bot;

    public ChangeCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        DiamondExchanger diamondExchanger = new DiamondExchanger();
        BotUser botUser = bot.getDatabase().getBotUser(executor.getIdLong());
        channel.sendMessage(new EmbedBuilder()
                .setTitle(":arrows_counterclockwise: Tauschen")
                .setDescription("Möchtest du wirklich einen Diamanten gegen 40 Münzen tauschen?")
                .setColor(Color.CYAN).build()).queue(msg -> {
            msg.addReaction(EmoteType.CHECKMARK.name).queue();
            msg.addReaction(EmoteType.CANCEL.name).queue();
            ReactionWaiter waiter = new ReactionWaiter(executor.getIdLong(), msg.getIdLong(), EmoteType.CHECKMARK, EmoteType.CANCEL);
            waiter.startWaiting();
            waiter.onEvent(event -> {
                if (event.getEmote().equals(EmoteType.CHECKMARK)) {
                    if (diamondExchanger.validateTransaction(botUser)) {
                        diamondExchanger.buy(bot, executor);
                        msg.editMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle(":arrows_counterclockwise: Erfolgreicher Tausch!")
                                .setDescription(diamondExchanger.getSuccessMessage()).build()).queue();
                    } else {
                        msg.editMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Fehler!")
                                .setDescription(diamondExchanger.getErrorMessage()).build()).queue();
                    }
                    msg.clearReactions().queue();
                } else {
                    msg.delete().queue();
                    message.delete().queue();
                }
                waiter.stopWaiting();
            }); // waiter
        }); // queue
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" + bot.getPrefix() + "tauschen`")
                .addField("Beschreibung:", "Tauscht einen Diamanten gegen 40 Münzen. ", false)
                .addField("Berechtigungslevel", PermissionLevel.MEMBER.name(), false)
                .addField("seit Version", "2.2.0", false)
                .build()).queue();
    }

}
