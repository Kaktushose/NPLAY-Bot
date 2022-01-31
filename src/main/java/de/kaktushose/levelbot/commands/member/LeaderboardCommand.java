package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.discord.reactionwaiter.ReactionWaiter;
import de.kaktushose.levelbot.database.services.LevelService;
import de.kaktushose.levelbot.util.Pagination;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.kaktushose.levelbot.util.Pagination.CurrencyType;

@CommandController({"rangliste", "leaderboard", "lb"})
public class LeaderboardCommand {

    private static final String BACK = "◀️";
    private static final String FORTH = "▶️";
    private static final String XP = "\uD83C\uDF1F";
    private static final String COINS = "\uD83D\uDCB0";
    private static final String DIAMONDS = "\uD83D\uDC8E";

    @Inject
    private LevelService levelService;
    @Inject
    private EmbedCache embedCache;
    private Guild guild;
    private CurrencyType currencyType;
    private Map<CurrencyType, Pagination> paginationMap;

    @Command(
            name = "Rangliste",
            usage = "{prefix}rangliste",
            desc = "Zeigt eine Rangliste der Benutzer mit den meisten XP, Münzen oder Diamanten",
            category = "Levelsystem"
    )
    public void onLeaderboard(CommandEvent event) {
        this.guild = event.getGuild();
        currencyType = CurrencyType.XP;
        paginationMap = new HashMap<>();
        paginationMap.put(CurrencyType.XP, levelService.getXpLeaderboard(10, event.getJDA()));
        paginationMap.put(CurrencyType.COINS, levelService.getCoinsLeaderboard(10, event.getJDA()));
        paginationMap.put(CurrencyType.DIAMONDS, levelService.getDiamondsLeaderboard(10, event.getJDA()));
        showLeaderboard(event, null);
    }

    public void showLeaderboard(CommandEvent event, Message sentMessage) {
        Pagination pagination = paginationMap.get(currencyType);
        Consumer<Message> success = message -> {
            addReactions(message, pagination.index(), pagination.pages());
            ReactionWaiter waiter = new ReactionWaiter(message, event.getMember(), BACK, FORTH, XP, COINS, DIAMONDS);
            waiter.onEvent(reactionEvent -> {
                switch (reactionEvent.getEmote()) {
                    case BACK:
                        if (pagination.index() == 0) {
                            return;
                        }
                        pagination.previousPage();
                        break;
                    case FORTH:
                        if (pagination.index() + 1 == pagination.pages()) {
                            return;
                        }
                        pagination.nextPage();
                        break;
                    case XP:
                        if (currencyType == CurrencyType.XP) {
                            return;
                        }
                        currencyType = CurrencyType.XP;
                        break;
                    case COINS:
                        if (currencyType == CurrencyType.COINS) {
                            return;
                        }
                        currencyType = CurrencyType.COINS;
                        break;
                    case DIAMONDS:
                        if (currencyType == CurrencyType.DIAMONDS) {
                            return;
                        }
                        currencyType = CurrencyType.DIAMONDS;
                        break;
                    default:
                        break;
                }
                showLeaderboard(event, message);
                waiter.stopWaiting(true);
            });
        };

        if (sentMessage == null) {
            event.reply(buildEmbed(pagination.getPage(), pagination.index(), pagination.pages()), success);
        } else {
            sentMessage.editMessage(buildEmbed(pagination.getPage(), pagination.index(), pagination.pages()).build()).queue(success);
        }
    }

    private EmbedBuilder buildEmbed(List<String> users, int index, int page) {
        EmbedBuilder embedBuilder = embedCache.getEmbed("leaderboard")
                .injectValue("guild", guild.getName())
                .injectValue("currency", currencyType.toString())
                .toEmbedBuilder();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            stringBuilder.append(String.format("`%d)` ", (i + 1) + index * 10)).append(users.get(i)).append("\n");
        }

        return embedBuilder.setDescription(stringBuilder.toString())
                .setFooter(String.format("Seite %d/%d", index + 1, page));
    }

    private void addReactions(Message message, int index, int pages) {
        if (index == 0) {
            message.addReaction(FORTH).queue();
        } else if (index + 1 == pages) {
            message.addReaction(BACK).queue();
        } else {
            message.addReaction(BACK).and(message.addReaction(FORTH)).queue();
        }
        switch (currencyType) {
            case XP:
                message.addReaction(COINS).and(message.addReaction(DIAMONDS)).queue();
                break;
            case COINS:
                message.addReaction(XP).and(message.addReaction(DIAMONDS)).queue();
                break;
            case DIAMONDS:
                message.addReaction(XP).and(message.addReaction(COINS)).queue();
                break;
        }
    }
}
