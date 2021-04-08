package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Optional;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.Item;
import de.kaktushose.levelbot.database.model.Rank;
import de.kaktushose.levelbot.database.service.LevelService;
import de.kaktushose.levelbot.database.service.UserService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

@CommandController({"info", "rank", "konto"})
public class RankInfoCommand {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private LevelService levelService;
    @Inject
    private UserService userService;

    @Command(
            name = "Kontoinformation abrufen",
            usage = "{prefix}info <member>",
            desc = "Zeigt die Kontoinformationen zu einem User an",
            category = "Levelsystem"
    )
    public void onRankInfo(CommandEvent event, @Optional Member member) {
        Member target = member == null ? event.getMember() : member;

        BotUser botUser = userService.getById(target.getIdLong());

        Rank currentRank = levelService.getCurrentRank(botUser.getUserId());
        Rank nextRank = levelService.getNextRank(botUser.getUserId());
        long nextRankXp = nextRank.getBound() - botUser.getXp();
        long xpGain = botUser.getXp() - botUser.getStartXp();
        long coinsGain = botUser.getCoins() - botUser.getStartCoins();
        long diamondsGain = botUser.getDiamonds() - botUser.getStartDiamonds();

        EmbedBuilder embedBuilder = embedCache.getEmbed("rankInfo")
                .injectValue("user", target.getAsMention())
                .injectValue("color", currentRank.getColor())
                .injectValue("currentRank", String.format("<@&%d>", currentRank.getRoleId()))
                .injectValue("nextRank", String.format("<@&%d> (noch %d XP)", nextRank.getRoleId(), nextRankXp))
                .injectValue("avatarUrl", target.getUser().getAvatarUrl())
                .injectValue("xpGain", xpGain)
                .injectValue("coinsGain", coinsGain)
                .injectValue("diamondsGain", diamondsGain)
                .injectFields(botUser)
                .toEmbedBuilder();

        if (botUser.getTransactions().isEmpty()) {
            embedBuilder.addField("Keine Items in Besitz", "", false);
        }

        botUser.getTransactions().forEach(transaction -> {
            Item item = transaction.getItem();
            embedBuilder.addField(item.getName(), item.getRemainingTimeAsDate(transaction.getBuyTime()), false);
        });

        event.reply(embedBuilder);
    }
}