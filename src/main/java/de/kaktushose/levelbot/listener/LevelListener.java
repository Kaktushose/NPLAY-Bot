package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.Rank;
import de.kaktushose.levelbot.database.services.LevelService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LevelListener extends ListenerAdapter {

    private final LevelService levelService;
    private final Levelbot levelbot;

    public LevelListener(Levelbot levelbot) {
        this.levelService = levelbot.getLevelService();
        this.levelbot = levelbot;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        User author = event.getAuthor();
        if (author.isBot()) {
            return;
        }
        if (!levelService.isValidMessage(author.getIdLong(), guild.getIdLong(), event.getChannel().getIdLong())) {
            return;
        }
        if (event.getMessage().getContentStripped().length() < 10) {
            return;
        }

        Optional<Rank> optional = levelService.onValidMessage(author.getIdLong());
        if (optional.isEmpty()) {
            return;
        }

        Rank currentRank = optional.get();
        Rank nextRank = levelService.getNextRank(author.getIdLong());
        TextChannel channel = levelbot.getBotChannel();
        String rewards = levelService.applyRewards(author.getIdLong(), currentRank.getRankId());

        levelbot.addRankRole(author.getIdLong(), currentRank.getRankId());
        levelbot.removeRankRole(author.getIdLong(), levelService.getPreviousRank(author.getIdLong()).getRankId());

        channel.sendMessage(author.getAsMention())
                .and(channel.sendMessage(levelbot.getEmbedCache().getEmbed("levelUp")
                        .injectValue("user", author.getAsMention())
                        .injectValue("color", currentRank.getColor())
                        .injectValue("currentRank", guild.getRoleById(currentRank.getRoleId()).getAsMention())
                        .injectValue("nextRank", guild.getRoleById(nextRank.getRoleId()).getAsMention())
                        .injectValue("reward", rewards)
                        .injectValue("xp", nextRank.getBound())
                        .toMessageEmbed())
                ).queue();
    }
}
