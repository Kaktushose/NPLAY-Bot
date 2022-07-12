package de.kaktushose.levelbot.account.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Button;
import com.github.kaktushose.jda.commands.annotations.interactions.Choices;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.data.StateSection;
import com.github.kaktushose.jda.commands.dispatching.ButtonEvent;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.interactions.components.Buttons;
import de.kaktushose.levelbot.leveling.data.LevelService;
import de.kaktushose.levelbot.util.Pagination;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.kaktushose.levelbot.util.Pagination.CurrencyType;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@CommandController(value = "rangliste", category = "Levelsystem", ephemeral = true)
public class LeaderboardCommand {

    private final StateSection section;
    @Inject
    private LevelService levelService;
    @Inject
    private EmbedCache embedCache;

    public LeaderboardCommand() {
        section = new StateSection(5, TimeUnit.MINUTES);
    }

    @Command(
            name = "Rangliste",
            usage = "{prefix}rangliste",
            desc = "Zeigt eine Rangliste der Benutzer mit den meisten XP, Münzen oder Diamanten"
    )
    public void onLeaderboard(CommandEvent event,
                              @Choices({"xp", "münzen", "diamanten"})
                              @Param(name = "währung", value = "Die Währung dessen Rangliste gezeigt werden soll")
                                      CurrencyType currencyType) {
        Pagination pagination = levelService.getLeaderboard(currencyType, 10, event.getJDA());
        event.with(Buttons.disabled("onBack"), Buttons.enabled("onForth")).reply(buildEmbed(pagination));
        section.put(event.getAuthor().getId(), pagination);
    }

    @Button(label = "Zurück")
    public void onBack(ButtonEvent event) {
        if (!section.contains(event.getAuthor().getId())) {
            event.clearComponents().edit(embedCache.getEmbed("interactionTimeout"));
            return;
        }
        Pagination pagination = section.get(event.getAuthor().getId(), Pagination.class).get();
        pagination.previousPage();
        if (pagination.index() == 0) {
            event.with(Buttons.disabled("onBack"), Buttons.enabled("onForth")).edit(buildEmbed(pagination));
        } else {
            event.with(Buttons.enabled("onBack", "onForth")).edit(buildEmbed(pagination));
        }
    }

    @Button(label = "Weiter")
    public void onForth(ButtonEvent event) {
        if (!section.contains(event.getAuthor().getId())) {
            event.clearComponents().edit(embedCache.getEmbed("interactionTimeout"));
            return;
        }
        Pagination pagination = section.get(event.getAuthor().getId(), Pagination.class).get();
        pagination.nextPage();
        if (pagination.index() + 1 == pagination.pages()) {
            event.with(Buttons.enabled("onBack"), Buttons.disabled("onForth")).edit(buildEmbed(pagination));
        } else {
            event.with(Buttons.enabled("onBack", "onForth")).edit(buildEmbed(pagination));
        }
    }

    private EmbedBuilder buildEmbed(Pagination pagination) {
        EmbedBuilder embedBuilder = embedCache.getEmbed("leaderboard")
                .injectFormat(pagination.getCurrencyType().toString())
                .toEmbedBuilder();

        List<String> users = pagination.getPage();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            stringBuilder.append(String.format("`%d)` ", (i + 1) + pagination.index() * 10)).append(users.get(i)).append("\n");
        }

        return embedBuilder.setDescription(stringBuilder.toString())
                .setFooter(String.format("Seite %d/%d", pagination.index() + 1, pagination.pages()));
    }

}
