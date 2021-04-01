package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import de.kaktushose.nrtv.discord.frameworks.command.SubCommand;
import de.kaktushose.nrtv.discord.frameworks.event.EventItem;
import de.kaktushose.nrtv.discord.frameworks.level.shop.Item;
import de.kaktushose.nrtv.discord.frameworks.level.shop.ItemType;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.EmoteType;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.ReactionWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShopCommand extends Command {

    private Bot bot;
    private  EventItem eventItem;
    private EmbedBuilder defaultShop, detailedShop, premiumShop, premiumUnlimitedShop, DJShop, nicknameShop, boosterShop, xpBoosterShop, successEmbed, errorEmbed;

    public ShopCommand(Bot bot) {
        this.bot = bot;
    }

    private void loadEmbeds() {
        defaultShop = new EmbedBuilder()
                .setTitle(":shopping_cart: Shop")
                .setDescription("**Sichere dir während der Ostertage 50% auf PREMIUM gold und 30% auf alle Münzenbooster! :small_red_triangle_down:**\n" +
                        "Eine genaue Übersicht über alle Items findest du in <#742880857391366256>")
                .setColor(Color.ORANGE)
                .addField(EmoteType.PREMIUM.name + " PREMIUM", "Erhalte mit dieser besonderen Rolle satte 12 exklusive Vorteile auf unserem Server!", false)
                .addField(EmoteType.DJ.name + " Rythm DJ Perk", "Mit der DJ Rolle für den \"Rythm\" bist Du der Star!", false)
                .addField(EmoteType.NICKNAME.name + " Nickname PERK", "Mit dem Nickname PERK erhältst du das Recht, Deinen Nicknamen auf dem Server selbstständig jederzeit zu ändern!", false)
                .addField(EmoteType.BOOSTER.name + " Münzenbooster", "Mit dem Münzenbooster erhältst Du +2 Münzen je gezählter Nachricht zusätzlich - optimal für viele Münzen!", false)
                .addField(EmoteType.XPBOOSTER.name + " XP-Booster", "Mit dem XP-Booster erhältst Du +2 XP je gezählter Nachricht zusätzlich - steige so leichter in neue Stufen auf!", false)
                .setFooter("Um ein Item zu kaufen, reagiere mit dem jeweiligen Emote!");

        eventItem = bot.getDatabase().getEventItem(bot.getPresentEventItem());

        if (bot.eventItemIsPresent()) {
            defaultShop.addField(EmoteType.EVENT.name + " " + eventItem.getName(), eventItem.getDescription(), false);
        }

        successEmbed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(":shopping_bags: Erfolgreicher Kauf!");

        errorEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Fehler!");

        Item item;

        premiumShop = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle(":shopping_cart: Shop")
                .setDescription("Für dieses Item stehen folgende Variationen zur Verfügung:")
                .setFooter("Um ein Item zu kaufen, reagiere mit dem jeweiligen Emote!");
        for (int i = 0; i < 3; i++) {
            item = bot.getDatabase().getItemType(i, ItemType.PREMIUM);
            premiumShop.addField(EmoteType.getNumber(i).name + ": " + item.getName(),
                    "Preis: " + item.getPrice() + " :moneybag:\n" + "Dauer: " + TimeUnit.MILLISECONDS.toDays(item.getDuration()) + " Tage",
                    false);
        }

        premiumUnlimitedShop = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle(":shopping_cart: Shop")
                .setDescription("Für dieses Item stehen folgende Variationen zur Verfügung:")
                .setFooter("Um ein Item zu kaufen, reagiere mit dem jeweiligen Emote!");
        for (int i = 0; i < 4; i++) {
            item = bot.getDatabase().getItemType(i, ItemType.PREMIUM);
            if (i == 3) {
                premiumUnlimitedShop.addField(EmoteType.getNumber(i).name + ": " + item.getName(),
                        "Preis: 0 :moneybag:\n" + "Dauer: unbegrenzt",
                        false);
            } else {
                premiumUnlimitedShop.addField(EmoteType.getNumber(i).name + ": " + item.getName(),
                        "Preis: " + item.getPrice() + " :moneybag:\n" + "Dauer: " + TimeUnit.MILLISECONDS.toDays(item.getDuration()) + " Tage",
                        false);
            }
        }

        DJShop = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle(":shopping_cart: Shop")
                .setDescription("Für dieses Item stehen folgende Variationen zur Verfügung:")
                .setFooter("Um ein Item zu kaufen, reagiere mit dem jeweiligen Emote!");
        for (int i = 0; i < 3; i++) {
            item = bot.getDatabase().getItemType(i, ItemType.DJ);
            DJShop.addField(EmoteType.getNumber(i).name + ": " + item.getName(),
                    "Preis: " + item.getPrice() + " :moneybag:\n" + "Dauer: " + TimeUnit.MILLISECONDS.toDays(item.getDuration()) + " Tage",
                    false);
        }

        nicknameShop = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle(":shopping_cart: Shop")
                .setDescription("Für dieses Item stehen folgende Variationen zur Verfügung:")
                .setFooter("Um ein Item zu kaufen, reagiere mit dem jeweiligen Emote!");
        for (int i = 0; i < 3; i++) {
            item = bot.getDatabase().getItemType(i, ItemType.NICKNAME);
            nicknameShop.addField(EmoteType.getNumber(i).name + ": " + item.getName(),
                    "Preis: " + item.getPrice() + " :moneybag:\n" + "Dauer: " + TimeUnit.MILLISECONDS.toDays(item.getDuration()) + " Tage",
                    false);
        }

        boosterShop = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle(":shopping_cart: Shop")
                .setDescription("Für dieses Item stehen folgende Variationen zur Verfügung:")
                .setFooter("Um ein Item zu kaufen, reagiere mit dem jeweiligen Emote!");
        for (int i = 0; i < 2; i++) {
            item = bot.getDatabase().getItemType(i, ItemType.BOOSTER);
            boosterShop.addField(EmoteType.getNumber(i).name + ": " + item.getName(),
                    "Preis: " + item.getPrice() + " :moneybag:\n" + "Dauer: " + TimeUnit.MILLISECONDS.toDays(item.getDuration()) + " Tage",
                    false);
        }

        xpBoosterShop  = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle(":shopping_cart: Shop")
                .setDescription("Für dieses Item stehen folgende Variationen zur Verfügung:")
                .setFooter("Um ein Item zu kaufen, reagiere mit dem jeweiligen Emote!");
        for (int i = 0; i < 2; i++) {
            item = bot.getDatabase().getItemType(i, ItemType.XPBOOSTER);
            xpBoosterShop.addField(EmoteType.getNumber(i).name + ": " + item.getName(),
                    "Preis: " + item.getPrice() + " :moneybag:\n" + "Dauer: " + TimeUnit.MILLISECONDS.toDays(item.getDuration()) + " Tage",
                    false);
        }

    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        loadEmbeds();
        sendDefaultShop(executor, Collections.singletonList(executor), channel, message, null, true, true);
    }

    @SubCommand(MEMBER_GROUP)
    public void buyForMember(Member executor, Arguments args, TextChannel channel, Message message) {
        loadEmbeds();
        if (bot.getDatabase().getBotUser(executor.getIdLong()).getPermissionLevel() < PermissionLevel.MODERATOR.level) {
            return;
        }

        List<Member> members = args.getAsMemberList(0);
        sendDefaultShop(executor, members, channel, message, null, true, false);
    }

    private void sendDefaultShop(Member executor, List<Member> targets, TextChannel channel, Message commandMessage, Message shopMessage, boolean newMessage, boolean selfBuy) {
        Message msg;
        List<EmoteType> emotes = new ArrayList<>(Arrays.asList(EmoteType.PREMIUM, EmoteType.DJ, EmoteType.NICKNAME, EmoteType.BOOSTER, EmoteType.XPBOOSTER));
        List<EmoteType> reactions = new ArrayList<>();
        reactions.add(EmoteType.ONE);
        reactions.add(EmoteType.TWO);
        reactions.add(EmoteType.THREE);
        if (bot.eventItemIsPresent()) {
            emotes.add(EmoteType.EVENT);
        }
        emotes.add(EmoteType.CANCEL);
        reactions.add(EmoteType.BACK);
        reactions.add(EmoteType.CANCEL);

        if (!selfBuy) {
            defaultShop.setDescription("Hinweis: Du kaufst für fremde User");
        }

        if (newMessage) {
            msg = channel.sendMessage(defaultShop.build()).complete();
        } else {
            shopMessage.clearReactions().queue();
            msg = shopMessage.editMessage(defaultShop.build()).complete();
        }
        emotes.forEach(emote -> msg.addReaction(emote.name).queue());

        ReactionWaiter reactionWaiter = new ReactionWaiter(executor.getIdLong(), msg.getIdLong(), emotes);
        reactionWaiter.startWaiting();
        reactionWaiter.onEvent(event -> {
            ItemType itemType;
            switch (event.getEmote()) {
                case PREMIUM:
                    if (selfBuy) {
                        detailedShop = premiumShop;
                    } else {
                        reactions.add(3, EmoteType.FOUR);
                        detailedShop = premiumUnlimitedShop;
                    }
                    itemType = ItemType.PREMIUM;
                    break;
                case DJ:
                    detailedShop = DJShop;
                    itemType = ItemType.DJ;
                    break;
                case NICKNAME:
                    detailedShop = nicknameShop;
                    itemType = ItemType.NICKNAME;
                    break;
                case BOOSTER:
                    reactions.remove(EmoteType.THREE);
                    detailedShop = boosterShop;
                    itemType = ItemType.BOOSTER;
                    break;
                case XPBOOSTER:
                    reactions.remove(EmoteType.THREE);
                    detailedShop = xpBoosterShop;
                    itemType = ItemType.XPBOOSTER;
                    break;
                case EVENT:
                    if (eventItem.validate(bot, executor)) {
                        eventItem.buy(bot, executor);
                        successEmbed.setDescription(eventItem.getSuccessMessage());
                        msg.editMessage(successEmbed.build()).queue();
                    } else {
                        errorEmbed.setDescription(eventItem.getErrorMessage());
                        msg.editMessage(errorEmbed.build()).queue(message -> {
                            message.addReaction(EmoteType.BACK.name).queue();
                            message.addReaction(EmoteType.CANCEL.name).queue();
                            ReactionWaiter waiter = new ReactionWaiter(executor.getIdLong(), message.getIdLong(), EmoteType.BACK, EmoteType.CANCEL);
                            waiter.startWaiting();
                            waiter.onEvent(event1 -> {
                                switch (event1.getEmote()) {
                                    case BACK:
                                        sendDefaultShop(executor, targets, channel, commandMessage, message, false, selfBuy);
                                        break;
                                    case CANCEL:
                                        commandMessage.delete().queue();
                                        message.delete().queue();
                                        break;
                                }
                                waiter.stopWaiting();
                            });
                        });
                    }
                    msg.clearReactions().queue();
                    reactionWaiter.stopWaiting();
                    return;
                case CANCEL:
                    commandMessage.delete().queue();
                    msg.delete().queue();
                    reactionWaiter.stopWaiting();
                    return;
                default:
                    reactionWaiter.stopWaiting();
                    return;
            }
            sendDetailedShop(executor, targets, channel, msg, reactions, itemType, commandMessage, selfBuy);
            reactionWaiter.stopWaiting();
        });
    }

    private void sendDetailedShop(Member executor, List<Member> targets, TextChannel channel, Message shopMessage, List<EmoteType> reactions, ItemType type, Message commandMessage, boolean selfBuy) {
        shopMessage.clearReactions().queue();
        shopMessage.editMessage(detailedShop.build()).queue(msg -> {

            reactions.forEach(emote -> shopMessage.addReaction(emote.name).queue());

            ReactionWaiter reactionWaiter = new ReactionWaiter(executor.getIdLong(), shopMessage.getIdLong(), new ArrayList<>(reactions));
            reactionWaiter.startWaiting();
            reactionWaiter.onEvent(event -> {
                Item item;
                switch (event.getEmote()) {
                    case ONE:
                        item = bot.getDatabase().getItemType(0, type);
                        break;
                    case TWO:
                        item = bot.getDatabase().getItemType(1, type);
                        break;
                    case THREE:
                        item = bot.getDatabase().getItemType(2, type);
                        break;
                    case FOUR:
                        item = bot.getDatabase().getItemType(3, type);
                        break;
                    case BACK:
                        sendDefaultShop(executor, targets, channel, commandMessage, shopMessage, false, selfBuy);
                        reactionWaiter.stopWaiting();
                        return;
                    case CANCEL:
                        msg.delete().queue();
                        commandMessage.delete().queue();
                        reactionWaiter.stopWaiting();
                        return;
                    default:
                        reactionWaiter.stopWaiting();
                        return;
                }
                reactionWaiter.stopWaiting();

                boolean success = false;
                List<Member> members = new ArrayList<>();

                for (Member member : targets) {
                    BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());

                    if (!selfBuy) {
                        botUser.setCoins(botUser.getCoins() + item.getPrice());
                        bot.getDatabase().setBotUser(botUser);
                    }

                    if (item.validateTransaction(botUser)) {
                        item.buy(bot, member);
                        successEmbed.setDescription(item.getSuccessMessage())
                                .addField("Dauer:", ":timer_clock: " + TimeUnit.MILLISECONDS.toDays(item.getDuration()) + " Tage", false);
                        members.add(member);
                        success = true;
                    } else {
                        errorEmbed.setDescription(item.getErrorMessage());
                    }
                }

                if (success && selfBuy) {
                    shopMessage.editMessage(successEmbed.build()).queue();
                    shopMessage.clearReactions().queue();
                } else if (selfBuy) {
                    shopMessage.editMessage(errorEmbed.build()).queue();
                    shopMessage.clearReactions().queue();
                    shopMessage.addReaction(EmoteType.BACK.name).queue();
                    shopMessage.addReaction(EmoteType.CANCEL.name).queue();
                    ReactionWaiter waiter = new ReactionWaiter(executor.getIdLong(), shopMessage.getIdLong(), EmoteType.BACK, EmoteType.CANCEL);
                    waiter.startWaiting();
                    waiter.onEvent(reactionEvent -> {
                        switch (reactionEvent.getEmote()) {
                            case CANCEL:
                                commandMessage.delete().queue();
                                shopMessage.delete().queue();
                                return;
                            case BACK:
                                shopMessage.clearReactions().queue();
                                sendDefaultShop(executor, targets, channel, commandMessage, shopMessage, false, selfBuy);
                                return;
                        }
                        waiter.stopWaiting();
                    });
                } else {
                    shopMessage.clearReactions().queue();
                    String memberNames;
                    System.out.println(members.size());
                    if (members.size() == 1) {
                        memberNames = members.get(0).getEffectiveName();
                    } else if (members.size() == 2) {
                        memberNames = members.get(0).getEffectiveName() + " und " + members.get(1).getEffectiveName();
                    } else if (members.size() == 0) {
                        shopMessage.editMessage(new EmbedBuilder()
                                .setTitle("Warnung!")
                                .setDescription("Es konnte keinem Member das Item gekauft werden!")
                                .setColor(Color.ORANGE).build()).queue();
                        return;
                    } else {
                         memberNames = members.get(0).getEffectiveName() + " und " + (members.size() - 1) + " weiteren Membern";
                    }

                    shopMessage.editMessage(new EmbedBuilder()
                            .setTitle("Erfolg!")
                            .setDescription(memberNames + " wurde das Item gekauft!")
                            .setColor(Color.GREEN).build()).queue();
                }
            });
        });
    }


    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" + bot.getPrefix() + "kaufen`")
                .addField("Beschreibung:", "Ermöglicht das Kaufen der Items, welche im <#648968903673905162> verfügbar sind\n" +
                        "Die Menüführung funktioniert mittels Reaktionen.", false)
                .addField("Berechtigungslevel", PermissionLevel.MEMBER.name(), false)
                .addField("seit Version", "2.0.0", false)
                .build()).queue();
    }


}

