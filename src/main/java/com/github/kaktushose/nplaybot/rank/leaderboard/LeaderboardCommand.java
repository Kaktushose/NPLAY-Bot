package com.github.kaktushose.nplaybot.rank.leaderboard;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Button;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.interactions.components.ComponentEvent;
import com.github.kaktushose.jda.commands.dispatching.reply.Replyable;
import com.github.kaktushose.jda.commands.dispatching.reply.components.Buttons;
import com.github.kaktushose.jda.commands.dispatching.reply.components.Component;
import com.github.kaktushose.nplaybot.Database;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;


@Interaction
public class LeaderboardCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    private final int minIndex = 1;
    private int index = 1;
    private int maxIndex;
    private List<LeaderboardPage> leaderboard;
    private Guild guild;

    @SlashCommand(value = "leaderboard", desc = "Zeigt eine Rangliste der Benutzer mit den meisten XP", isGuildOnly = true)
    public void onCommand(CommandEvent event) {
        guild = event.getGuild();
        leaderboard = database.getRankService().getLeaderboard();
        maxIndex = leaderboard.size();
        reply(event);
    }

    @Button(emoji = "⏪")
    public void onStart(ComponentEvent event) {
        index = 1;
        reply(event);
    }

    @Button(emoji = "◀️")
    public void onBackward(ComponentEvent event) {
        if (index > minIndex) {
            index--;
        }
        reply(event);
    }

    @Button(emoji = "▶️")
    public void onForward(ComponentEvent event) {
        if (index < maxIndex) {
            index++;
        }
        reply(event);
    }

    @Button(emoji = "⏩")
    public void onEnd(ComponentEvent event) {
        index = maxIndex;
        reply(event);
    }

    private void reply(Replyable event) {
        event.with(getButtons()).reply(
                embedCache.getEmbed("leaderboard").injectValue("leaderboard", leaderboard.get(index - 1).getPage(guild))
                        .toEmbedBuilder()
                        .setFooter(String.format("Seite (%d/%d)", index, maxIndex))
        );
    }

    private Component[] getButtons() {
        if (index == minIndex) {
            return List.of(
                    Buttons.disabled("onStart", "onBackward"),
                    Buttons.enabled("onForward", "onEnd")
            ).toArray(new Component[0]);
        }
        if (index == maxIndex) {
            return List.of(
                    Buttons.enabled("onStart", "onBackward"),
                    Buttons.disabled("onForward", "onEnd")
            ).toArray(new Component[0]);
        }
        return List.of(
                Buttons.enabled("onStart", "onBackward"),
                Buttons.enabled("onForward", "onEnd")
        ).toArray(new Component[0]);
    }
}
