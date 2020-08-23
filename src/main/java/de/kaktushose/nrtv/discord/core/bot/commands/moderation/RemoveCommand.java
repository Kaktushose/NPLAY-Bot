package de.kaktushose.nrtv.discord.core.bot.commands.moderation;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.*;
import de.kaktushose.nrtv.discord.frameworks.level.shop.Item;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.EmoteType;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.ReactionWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Permissions(PermissionLevel.MODERATOR)
public class RemoveCommand extends Command {

    private Bot bot;

    public RemoveCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("Syntaxfehler!")
                .addField("Syntax:", "`" + bot.getPrefix() + "remove <@Member>`", false)
                .setColor(Color.ORANGE)
                .build()).queue();
    }

    @SubCommand(MEMBER_MENTION)
    public void onRemoveItem(Member executor, Arguments args, TextChannel channel, Message message) {
        Member member = args.getAsMember(0);
        BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
        EmbedBuilder builder = new EmbedBuilder().setColor(Color.ORANGE)
                .setTitle("Item entfernen")
                .setDescription(member.getAsMention() + " besitzt folgende Items:")
                .setFooter("Reagiere mit dem entsprechenden Emote, um ein Item zu entfernen");
        List<EmoteType> emotes = new ArrayList<>();
        AtomicInteger i = new AtomicInteger(0);

        List<Item> items = new ArrayList<>(botUser.getItemStack().values());

        items.forEach(item -> {
            EmoteType emoteType;
            emoteType = EmoteType.getNumber(i.getAndIncrement());
            emotes.add(emoteType);
            builder.addField(emoteType.name + ": " + item.getName(), "", false);
        });

        channel.sendMessage(builder.build()).queue(msg -> {
            emotes.forEach(emoteType -> msg.addReaction(emoteType.name).queue());
            ReactionWaiter waiter = new ReactionWaiter(executor.getIdLong(), msg.getIdLong(), emotes);
            waiter.startWaiting();
            waiter.onEvent(event -> {
                Item item;
                switch (event.getEmote()) {
                    case ONE:
                        item = items.get(0);
                        break;
                    case TWO:
                        item = items.get(1);
                        break;
                    case THREE:
                        item = items.get(2);
                        break;
                    case FOUR:
                        item = items.get(3);
                        break;
                    case FIVE:
                        item = items.get(4);
                        break;
                    default:
                        return;
                }
                waiter.stopWaiting();
                botUser.getItemStack().remove(item.getItemType());
                botUser.setBuyTime(0, item.getItemType());
                bot.getDatabase().setBotUser(botUser);
                switch (item.getItemType()) {
                    case DJ:
                        bot.removeRole(member, Bot.Roles.DJ);
                        break;
                    case NICKNAME:
                        bot.removeRole(member, Bot.Roles.NICKNAME);
                        break;
                    case PREMIUM:
                        bot.removeRole(member, Bot.Roles.PREMIUM);
                        break;
                }
                msg.clearReactions().queue();
                msg.editMessage(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Erfolg!")
                        .setDescription(member.getAsMention() + " wurde das Item " + item.getName() + " entfernt")
                        .build()).queue();
            });
        });

    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" +  bot.getPrefix() + "remove <@Member>`")
                .addField("Beschreibung:", "Entfernt ein Item aus dem Besitz eines Users.", false)
                .addField("Berechtigungslevel", PermissionLevel.MODERATOR.name(), false)
                .addField("seit Version", "2.1.0", false)
                .build()).queue();
    }
}
















