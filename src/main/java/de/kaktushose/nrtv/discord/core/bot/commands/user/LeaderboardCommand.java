package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.EmoteType;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.ReactionWaiter;
import de.kaktushose.nrtv.discord.util.Leaderboard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderboardCommand extends Command {

    private final Bot bot;
    private AtomicInteger page, position;
    private Leaderboard leaderBoard;
    private Guild guild;
    private EmbedBuilder builder;
    // 0 = xp, 1 = coins, 2 = diamond
    private int type;

    public LeaderboardCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        showLeaderboard(bot.getDatabase().getXpLeaderBoard(), channel, executor, null, 0);
    }

    private void showLeaderboard(Leaderboard leaderBoard, TextChannel channel, Member executor, Message message, int type) {
        position = new AtomicInteger(1);
        page = new AtomicInteger(0);
        guild = channel.getGuild();
        builder = new EmbedBuilder().setColor(Color.GRAY)
                .setTitle("Rangliste: " + guild.getName());
        this.leaderBoard = leaderBoard;
        this.type = type;
        loadPage(page.get());
        if (message == null) {
            channel.sendMessage(builder.build()).queue(msg -> afterMessage(msg, executor));
        } else {
            message.editMessage(builder.build()).queue(msg -> afterMessage(msg, executor));
        }
    }

    private void afterMessage(Message message, Member executor) {
        boolean bof = page.get() == 0;
        boolean eof = (page.get() + 1) == leaderBoard.getListSize();
        if (!bof) {
            message.addReaction(EmoteType.BACK.name).queue();
        }
        if (!eof) {
            message.addReaction(EmoteType.FORTH.name).queue();
        }
        switch (type) {
            case 0:
                message.addReaction(EmoteType.COINS.name).queue();
                message.addReaction(EmoteType.DIAMOND.name).queue();
                break;
            case 1:
                message.addReaction(EmoteType.XP.name).queue();
                message.addReaction(EmoteType.DIAMOND.name).queue();
                break;
            case 2:
                message.addReaction(EmoteType.COINS.name).queue();
                message.addReaction(EmoteType.XP.name).queue();
                break;
        }

        ReactionWaiter waiter = new ReactionWaiter(executor.getIdLong(), message.getIdLong(), EmoteType.BACK, EmoteType.FORTH, EmoteType.XP, EmoteType.DIAMOND, EmoteType.COINS);
        waiter.startWaiting();
        waiter.onEvent(event -> {
            switch (event.getEmote()) {
                case BACK:
                    if (!bof) {
                        position.set(page.get() * 10 - 9);
                        loadPage(page.decrementAndGet());
                        message.editMessage(builder.build()).queue();
                        message.clearReactions().queue();
                        waiter.stopWaiting();
                        afterMessage(message, executor);
                    }
                    break;
                case FORTH:
                    if (!eof) {
                        loadPage(page.incrementAndGet());
                        message.editMessage(builder.build()).queue();
                        message.clearReactions().complete();
                        waiter.stopWaiting();
                        afterMessage(message, executor);
                    }
                    break;
                case XP:
                    page.set(0);
                    position.set(1);
                    message.clearReactions().queue();
                    waiter.stopWaiting();
                    showLeaderboard(bot.getDatabase().getXpLeaderBoard(), message.getTextChannel(), executor, message, 0);
                    break;
                case COINS:
                    page.set(0);
                    position.set(1);
                    message.clearReactions().queue();
                    waiter.stopWaiting();
                    showLeaderboard(bot.getDatabase().getCoinsLeaderBoard(), message.getTextChannel(), executor, message, 1);
                    break;
                case DIAMOND:
                    page.set(0);
                    position.set(1);
                    message.clearReactions().queue();
                    waiter.stopWaiting();
                    showLeaderboard(bot.getDatabase().getDiamondsLeaderBoard(), message.getTextChannel(), executor, message, 2);
                    break;
            }
            waiter.stopWaiting();
        });
    }

    private void loadPage(int page) {
        StringBuilder sb = new StringBuilder();
        leaderBoard.getPage(page).forEach(id -> {
            BotUser botUser = bot.getDatabase().getBotUser(id);
            String currency = "undefined";
            int value = 0;
            switch (type) {
                case 0:
                    value = botUser.getXp();
                    currency = "XP";
                    break;
                case 1:
                    value = botUser.getCoins();
                    currency = "Münzen";
                    break;
                case 2:
                    value = botUser.getDiamonds();
                    currency = "Diamanten";
                    break;
            }
            sb.append(
                    String.format("`%d)` %s (%d %s)",
                            position.getAndIncrement(),
                            guild.getMemberById(botUser.getId()).getAsMention(),
                            value,
                            currency));
            sb.append("\n");
        });
        builder.setDescription(sb.toString())
                .setFooter(String.format("Seite (%d/%d)", (page + 1), leaderBoard.getListSize()));

    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" + bot.getPrefix() + "rangliste`")
                .addField("Beschreibung:", "Zeigt eine Rangliste der User mit den meisten XP. " +
                        "Die Liste ist absteigend sortiert und kann über Reaktionen durchgeblättert werden.", false)
                .addField("Berechtigungslevel", PermissionLevel.MEMBER.name(), false)
                .addField("seit Version", "2.1.0", false)
                .build()).queue();
    }
}